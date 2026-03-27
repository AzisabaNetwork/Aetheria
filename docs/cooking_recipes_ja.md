# 料理レシピ（日本語）

出典: `plugins/plugin-cooking/src/main/kotlin/net/azisaba/vanilife/cooking/CookingItems.kt`

以下は、`CookingItems.kt` の「Other（調理済み等）」カテゴリ項目ごとの簡易レシピです。ゲーム内の設計用に量は概算です。

- カレーライス (Curry Rice)
  - 材料: `RICE`（米）, `ONION`（玉ねぎ）, 人参*（にんじん）, じゃがいも*（じゃがいも）, 肉代用（`SAUSAGE`等）*, うま味として `MISO`（味噌）またはカレールウ代替
  - 手順: `ONION` を炒めて透き通らせる。角切り野菜と肉代用品を加え軽く焼く。水と `MISO`（またはルウ）を加えて煮込み、野菜が柔らかくなったら `STEAMED_RICE` にかけて供する。

- 筍ご飯 (Bamboo Shoot Rice)
  - 材料: `RICE`（米）, `BAMBOO_SHOOT`（筍）, 薄口の調味（または `MISO`）*, `BUTTER`（バター, 任意）
  - 手順: `RICE` を研ぐ。`BAMBOO_SHOOT` を薄切りにして `BUTTER` で軽く炒める。米と筍、調味を一緒に炊いて温かいうちに供する。

- うな丼 (Eel Rice Bowl)
  - 材料: `STEAMED_RICE`（ご飯）, `PIKE_CONGER`（鱧）や代替魚（`GRILLED_AYU` 等）*, たれ（`MISO` ベースで代用可）*
  - 手順: 魚を焼き、照り焼きだれを塗る。熱々の `STEAMED_RICE` にのせて供する。

- アジフライ (Fried Horse Mackerel)
  - 材料: 小魚（`SARDINE` や同等の魚）, 衣（パン粉代替に `BUCKWHEAT` など）*, 油または `BUTTER`
  - 手順: 片栗粉/卵/パン粉（代替）で衣をつけて揚げる。`LETTUCE` や `TOMATO` のサラダと一緒に出す。

- 鮎の塩焼き (Grilled Ayu)
  - 材料: 魚（`FIREFLY_SQUID` 代用可）*, 塩
  - 手順: 塩を振って炭火または網で焼き、皮が香ばしくなるまで焼く。丸ごと供する。

- 鯖の塩焼き (Grilled Mackerel)
  - 材料: 魚（`SARDINE` や `SKIPJACK_TUNA` 代用）*, 塩, 柑橘（`ORANGE` を代用可）
  - 手順: 塩を振って焼き、柑橘を添えて供する。

- さんまの塩焼き (Grilled Pacific Saury)
  - 材料: 魚（代用可）, 塩, 大根おろし（`JAPANESE_RADISH` をすりおろし）
  - 手順: 塩を振って焼き、大根おろしを添える。

- イカ焼き (Grilled Squid)
  - 材料: `FIREFLY_SQUID` または `SQUID_SUSHI`（生イカ）*, たれ（`MISO`＋砂糖で代用可）*, 柑橘類（任意）
  - 手順: イカを焼いてたれを塗り、適宜切って供する。

- ハンバーグ (Hamburg Steak)
  - 材料: 挽き肉代替（`SAUSAGE` を刻む等）*, `ONION`（玉ねぎ）, `BUTTER`, `CHEESE`（任意）
  - 手順: みじん切りの `ONION` を炒め、肉代替と混ぜて成形し `BUTTER` で焼く。好みで `CHEESE` を載せる。

- マリネした茄子 (Marinated Eggplant)
  - 材料: `EGGPLANT`（なす）, `ONION`, ビネガー代替（`ORANGE` ジュース少量）*, 油（`BUTTER` 少量でも可）
  - 手順: 茄子を焼くか炒めて切り、スライス玉ねぎとビネガー系のマリネ液に漬ける。

- 鯖の味噌煮 (Miso Mackerel)
  - 材料: `MISO`（味噌）, `SARDINE`（または鯖代替）, `ONION` や `GREEN_ONION`
  - 手順: 味噌、水、砂糖（代替）で煮汁を作り、魚を煮て照りが出るまで煮る。

- 味噌汁 (Miso Soup)
  - 材料: `MISO`（味噌）, 豆腐*（任意）, 海藻*（わかめ等）, `GREEN_ONION`（ねぎ）
  - 手順: だし（海藻から）を用意し、具材を煮て最後に味噌を溶かす。

- 肉じゃが (Nikujaga)
  - 材料: じゃがいも*（代替）, `ONION`, 肉代替（`SAUSAGE` 等）*, 調味（`MISO`／醤油系代替）
  - 手順: 肉と野菜を甘辛い出汁で煮込み、具材が柔らかくなるまで煮る。

- おでん (Oden)
  - 材料: 練り物や具材（`FRIED_HORSE_MACKEREL` 等を代用）, `JAPANESE_RADISH`（大根）, ゆで卵*（任意）
  - 手順: だしで長時間煮込み、味を染み込ませて供する。

- パフェ (Parfait)
  - 材料: `STRAWBERRY` / `BANANA` / `BLUEBERRY`, `SOFT_SERVE_ICE_CREAM` または `COTTON_CANDY` トッピング
  - 手順: グラスにアイスと果物、トッピングを層にして盛る。

- サラダ (Salad)
  - 材料: `LETTUCE`（レタス）, `TOMATO`（トマト）, `CUCUMBER`（きゅうり）, `ONION`, 任意で `CHEESE` や `SAUSAGE`
  - 手順: 全ての材料をカットしてドレッシングで和える。

- いくら寿司 (Salmon Roe Sushi)
  - 材料: `STEAMED_RICE`（ご飯）, `SALMON_ROE`（いくら）
  - 手順: 酢飯または `STEAMED_RICE` に `SALMON_ROE` を載せて供する。

- ソーセージ (Sausage)
  - 材料: `SAUSAGE`（そのまま）
  - 手順: 焼くか炒めてスライスし、玉ねぎやピーマンと合わせる。

- うに寿司 (Sea Urchin Sushi)
  - 材料: `STEAMED_RICE`, `SEA_URCHIN_SUSHI`（うに）
  - 手順: 小さく握ったご飯にうにをのせて供する。

- 海鮮丼 (Seafood Rice Bowl)
  - 材料: `STEAMED_RICE`, `SALMON_ROE`, `TUNA_SUSHI` や `SQUID_SUSHI` の切り身, `GREEN_ONION`
  - 手順: ご飯の上に海鮮を並べ、薬味を添えて供する。

- かき氷 (Shaved Ice)
  - 材料: `SHAVED_ICE`（かき氷）, 果実シロップ（`STRAWBERRY` / `MELON` 等）*, 練乳*（任意）
  - 手順: かき氷にシロップと好みのトッピングをかける。

- そば (Soba)
  - 材料: `BUCKWHEAT`（蕎麦）, `GREEN_ONION`, つけ汁（だし／`MISO` ベース代用）
  - 手順: 麺を茹で、つけ汁で食べるか温かい汁で供する。

- ソフトクリーム (Soft Serve Ice Cream)
  - 材料: `SOFT_SERVE_ICE_CREAM`, フルーツトッピング
  - 手順: コーンに盛り、トッピングを載せる。

- いか寿司 (Squid Sushi)
  - 材料: `STEAMED_RICE`, `SQUID_SUSHI`（いか）
  - 手順: 酢飯の上にいかをのせて供する。

- ご飯 (Steamed Rice)
  - 材料: `RICE`
  - 手順: 米を研ぎ、蒸し炊きにして炊飯する。

- たこ焼き (Takoyaki)
  - 材料: `TAKOYAKI`（準備済み）または生地（`BUCKWHEAT` を代用可）, `SQUID_SUSHI`（またはたこ代替）*, `GREEN_ONION`
  - 手順: たこ焼き器で生地を注ぎ、具を入れて丸く焼く。

- 玉子寿司 (Tamago Sushi)
  - 材料: `STEAMED_RICE`, `TAMAGO_SUSHI`（甘い玉子焼き）
  - 手順: 玉子を切り、ご飯の上に載せる。

- 照り焼き (Teriyaki Yellowtail)
  - 材料: `TERIYAKI_YELLOWTAIL`（準備済み）または `SKIPJACK_TUNA` / `PIKE_CONGER` に照り焼きだれを塗る
  - 手順: 焼きながらたれを塗り、照りを出す。

- とんかつ (Tonkatsu)
  - 材料: `TONKATSU`（準備済み）または代用のカツ（`SAUSAGE` を薄くして衣づけ）*, `LETTUCE`/千切りキャベツ代用
  - 手順: 衣をつけて揚げ、スライスして提供する。

- まぐろ寿司 (Tuna Sushi)
  - 材料: `STEAMED_RICE`, `TUNA_SUSHI`
  - 手順: 酢飯の上にまぐろの切り身をのせる。

- うどん (Udon)
  - 材料: `UDON`, `GREEN_ONION`, 汁（`MISO`／だし代用）
  - 手順: 汁を温め、麺を入れて提供する。

- 焼きそば (Yakisoba)
  - 材料: `YAKISOBA`（麺）, `LETTUCE`/キャベツ代用, `ONION`, `SAUSAGE` またはソース*
  - 手順: 野菜と麺を炒め、ソースで味付けする。

注意:
- `*` 印は `CookingItems.kt` で明示されていない一般的な材料（例: にんじん、じゃがいも、豆腐、練乳 等）。必要に応じて `CookingItems` 内の既存アイテムで代替することができます。
- これらはゲーム内の調理・合成設計向けのスケッチです。量、クラフトグリッド配置、JSONレシピ形式などが必要なら指定してください。
