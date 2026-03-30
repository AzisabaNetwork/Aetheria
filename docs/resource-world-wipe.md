# 資源ワールド ワイプ仕様

## 概要
このドキュメントは Vanilife の「資源ワールド自動ワイプ（季節切り替え）」の仕組みをまとめたものです。
季節が変わるとサーバーは**再起動なし**で資源ワールドを閉じ、ディスクから削除し、新しいワールドを生成します（ホットスワップ）。

---

## プレイヤー向け

### 何が起きるか
- 季節（SPRING / SUMMER / FALL / WINTER）が切り替わると、資源ワールドが自動的にリセットされます。
- ワイプ開始時、資源ワールドにいるプレイヤーはオーバーワールドのスポーンに自動テレポートされます。
- ワイプ中（数秒〜数十秒）は資源ワールドに入れません。
- ワイプ完了後、新しい資源ワールドが生成され、再び入れるようになります。

### 季節スケジュール
| 季節 | 期間 |
|------|------|
| SPRING | 3月〜5月 |
| SUMMER | 6月〜8月 |
| FALL | 9月〜11月 |
| WINTER | 12月〜2月 |

### 注意点
- ワイプ後、資源ワールド内のすべての建築物・アイテムが消えます。貴重品は島に持ち帰ってください。
- ベッド位置の記録もリセットされます（帰還先が島スポーンに戻る可能性があります）。

---

## 開発者向け（実装・内部仕様）

### 設計要点
- **Folia 対応**: `CraftServer.unloadWorld()` は Folia で `UnsupportedOperationException` を投げるため、NMS を直接操作します。
- **安定キー**: LevelStem キーは `vanilife:resource`（年ごとに変えない）。ワールド名は `world_vanilife_resource`。
- **状態マシン**: `WorldLifecycleState` で遷移を管理し、`AtomicReference<WorldLifecycleState>` + `compareAndSet` でスレッドセーフに制御。
- **スレッドモデル**: Folia のスレッド制約（TickThread 要求、リージョンスレッド要求）に従い、各フェーズを適切なスレッドで実行。

### ライフサイクル状態遷移

```
ACTIVE → DRAINING → CLOSING → DELETED → CREATING → ACTIVE
```

| 状態 | 説明 |
|------|------|
| `ACTIVE` | ワールドが稼働中。プレイヤーが入れる。 |
| `DRAINING` | プレイヤーをテレポートで退出させている。新規入場を拒否。 |
| `CLOSING` | チャンクシステム停止・データ保存・レジストリ除去中。 |
| `DELETED` | ワールドファイルがディスクから削除済。ServerLevel は存在しない。 |
| `CREATING` | 新しい ServerLevel を生成し、初期チャンクを読み込み中。 |

### スレッドモデル（実行フロー）

```
[WorldWiper-Scheduler daemon] 1時間ごとの季節チェック
        │
        │ Season.now() != currentSeason を検知
        ▼
[GlobalRegionScheduler] PreSeasonChangeEvent 発火 → drainPlayers()
        │
        │ CompletableFuture<Void> 完了（全プレイヤーテレポート済）
        ▼
[TickThread "ResourceWorldClose"] closeWorld() — チャンク停止、保存、レジストリ除去
        │
        ▼
[WorldWiper-Scheduler daemon] deleteWorldFolder() — ファイル削除（1秒遅延）
        │
        ▼
[GlobalRegionScheduler] createFreshWorld() → PostSeasonChangeEvent 発火
```

### 主要クラス

#### `WorldWiper`
場所: `folia-server/src/main/java/net/azisaba/vanilife/server/world/wiper/WorldWiper.java`

季節ワイプのオーケストレーター。

- `registerTask()`: サーバー起動時に呼び出される（`MinecraftServer` の `ServerLoadEvent` 後）。1時間ごとの季節チェックを登録。
- `forceSwap(Season)`: 管理コマンド用。任意の季節への強制スワップをトリガー。進行中のスワップがあれば `false` を返す。
- `getState()`: 現在の `WorldLifecycleState` を取得。

スケジューリングには `java.util.concurrent.ScheduledExecutorService`（デーモンスレッド `WorldWiper-Scheduler`）を使用。Plugin インスタンスは Folia の `GlobalRegionScheduler` 呼び出し時に遅延解決。

#### `ResourceWorldLifecycle`
場所: `folia-server/src/main/java/net/azisaba/vanilife/server/world/wiper/ResourceWorldLifecycle.java`

NMS レベルのヘルパー。4つの public メソッド:

| メソッド | スレッド要件 | 説明 |
|----------|-------------|------|
| `drainPlayers()` | Folia リージョンスレッド | 資源ワールド内の全プレイヤーをオーバーワールドスポーンへテレポート |
| `closeWorld(ServerLevel)` | TickThread | チャンクシステム停止、レジストリ除去、ストレージクローズ |
| `deleteWorldFolder()` | 任意スレッド | `dimensions/vanilife/resource` フォルダを再帰削除 |
| `createFreshWorld()` | サーバーメインスレッド / GlobalRegion | PaperWorldLoader と同じシーケンスで新 ServerLevel を生成 |

**リフレクション使用箇所**:
- `RegionizedServer.worlds` (private `CopyOnWriteArrayList<ServerLevel>`) — `removeWorld()` メソッドが存在しないため
- `CraftServer.worlds` (private `Map<String, World>`) — public な remove API がないため

#### `WorldLifecycleState`
場所: `folia-server/src/main/java/net/azisaba/vanilife/server/world/wiper/WorldLifecycleState.java`

5状態の列挙型。上記の状態遷移表を参照。

#### 既存イベント（変更なし）
- `PreSeasonChangeEvent`: ワイプ開始前に発火。`@Nullable oldSeason` と `newSeason` フィールド。
- `PostSeasonChangeEvent`: 新ワールド生成完了後に発火。同フィールド。

### ブートストラップフック
場所: `folia-server/src/minecraft/java/net/minecraft/server/MinecraftServer.java`

`ServerLoadEvent` 発火後、`this.connection.acceptConnections()` の直前に `WorldWiper.registerTask()` を呼び出し:

```java
// Vanilife - resource world season check
net.azisaba.vanilife.server.world.wiper.WorldWiper.registerTask();
```

### ワールドクローズの詳細シーケンス

```java
// 1. RegionizedServer からティックループを停止
RegionizedServer.getInstance().worlds.remove(serverLevel);  // リフレクション

// 2. チャンクシステムをソフト停止
serverLevel.moonrise$getChunkTaskScheduler().halt(false, 0L);

// 3. チャンクシステムをハード停止 + 保存
serverLevel.moonrise$getChunkTaskScheduler().chunkHolderManager.close(
    true,   // save
    true,   // halt
    true,   // first
    true,   // last
    false   // checkRegions
);

// 4. レベルデータ保存
serverLevel.saveLevelData(true);

// 5. MinecraftServer.levels マップから除去
server.removeLevel(serverLevel);

// 6. CraftServer.worlds マップから除去（リフレクション）
craftServer.worlds.remove("world_vanilife_resource");

// 7. ストレージアクセスをクローズ
serverLevel.levelStorageAccess.close();
```

### ワールド生成の詳細シーケンス

```java
// 1. LevelStem をレジストリから取得
Registry<LevelStem> stemRegistry = server.worldLoaderContext
    .datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM);
LevelStem stem = stemRegistry.getValueOrThrow(VanilifeLevelStems.RESOURCE);

// 2. ストレージアクセスを作成
LevelStorageAccess access = LevelStorageSource
    .createDefault(worldContainer)
    .validateAndCreateAccess("world_vanilife_resource", VanilifeLevelStems.RESOURCE);

// 3. 新規ワールドデータを作成
PrimaryLevelData data = Main.createNewWorldData(
    dedicatedServer.settings, server.worldLoaderContext,
    stemRegistry, false, false
).cookie();

// 4. ServerLevel 作成 + 各レジストリに登録
server.createLevel(stem, loadingInfo, access, data);

// 5. 初期チャンク読み込み + RegionizedServer 登録 + WorldLoadEvent 発火
server.prepareLevel(newLevel);
```

### エラー処理

各フェーズで例外が発生した場合:
- エラーをログ出力（`java.util.logging.Logger` — `Vanilife/WorldWiper`, `Vanilife/ResourceWorldLifecycle`）
- 状態を `ACTIVE` にリセット（半壊状態の世界が残る可能性あり。ログ監視推奨）
- 次回の1時間チェックで再試行される

### 関連ファイル

| ファイル | 役割 |
|----------|------|
| `folia-server/.../wiper/WorldWiper.java` | オーケストレーター |
| `folia-server/.../wiper/ResourceWorldLifecycle.java` | NMS ヘルパー |
| `folia-server/.../wiper/WorldLifecycleState.java` | 状態列挙型 |
| `folia-server/.../wiper/PreSeasonChangeEvent.java` | ワイプ前イベント |
| `folia-server/.../wiper/PostSeasonChangeEvent.java` | ワイプ後イベント |
| `folia-api/.../Season.java` | 季節判定 (`now()`, `resourceWorldKey()`) |
| `folia-api/.../Vanilife.java` | `getResourceWorld()` / `getResourceWorldOrNull()` |
| `folia-server/.../VanilifeLevelStems.java` | `RESOURCE` キー定義、`isResourceLevel()` |
| `folia-server/.../world/ResourceWorldImpl.java` | CraftWorld サブクラス |
| `plugins/plugin-portal/.../ExitForcer.kt` | 遅延ワールド解決（ホットスワップ対応） |

### 管理者向けメモ
- ワイプログは `Vanilife/WorldWiper` と `Vanilife/ResourceWorldLifecycle` ロガー名で出力されます。
- `WorldWiper.forceSwap(Season)` を使えば、季節に関係なく手動でワイプを実行できます（管理コマンドから呼び出し可能）。
- ワイプ中の状態は `WorldWiper.getState()` で確認できます。

---

## FAQ

**Q: サーバー再起動は必要？**
**A:** 不要。ホットスワップで実行されます。

**Q: ワイプ中にプレイヤーが資源ワールドにテレポートしようとしたら？**
**A:** `WorldLifecycleState` が `ACTIVE` 以外のとき、テレポート/ポータルのガード処理で拒否されるべきです（呼び出し側で `WorldWiper.getState()` をチェックしてください）。

**Q: ワイプが途中で失敗したら？**
**A:** 状態が `ACTIVE` にリセットされ、エラーがログに出力されます。次回の1時間チェック（または手動 `forceSwap`）で再試行されます。ワールドファイルが半端に残った場合は手動クリーンアップが必要な場合があります。

**Q: 管理者が手動でワイプしたい場合は？**
**A:** `WorldWiper.forceSwap(targetSeason)` を呼び出すコマンドを実装してください。既にスワップ進行中であれば `false` が返ります。
