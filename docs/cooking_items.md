# Cooking items

Source: `plugins/plugin-cooking/src/main/kotlin/net/azisaba/vanilife/cooking/CookingItems.kt`

This document lists all items declared in `CookingItems.kt` grouped to help think about recipes.

**Ingredients**
- `BAMBOO_SHOOT` (`bamboo_shoot`) — Category: `VEGETABLE` — Peak season: `SPRING`
- `BANANA` (`banana`) — Category: `FRUIT` — Peak season: `SUMMER (mid/late), FALL (early)`
- `BELL_PEPPER` (`bell_pepper`) — Category: `VEGETABLE` — Peak season: `SUMMER`
- `BLUEBERRY` (`blueberry`) — Category: `FRUIT` — Peak season: `SUMMER`
- `BUTTER` (`butter`) — Category: `MATERIAL`
- `BUCKWHEAT` (`buckwheat`) — Category: `MATERIAL`
- `CHEESE` (`cheese`) — Category: `MATERIAL`
- `CHERRY` (`cherry`) — Category: `FRUIT` — Peak season: `SPRING`
- `CHILI_PEPPER` (`chili_pepper`) — Category: `VEGETABLE` — Peak season: `SUMMER/FALL (mid)`
- `COFFEE_BEANS` (`coffee_beans`) — Category: `MATERIAL`
- `CORN` (`corn`) — Category: `VEGETABLE` — Peak season: `SUMMER`
- `CUCUMBER` (`cucumber`) — Category: `VEGETABLE` — Peak season: `SUMMER`
- `DRIED_PERSIMMON` (`dried_persimmon`) — Category: `FRUIT`
- `EGGPLANT` (`eggplant`) — Category: `VEGETABLE` — Peak season: `SUMMER`
- `FIREFLY_SQUID` (`firefly_squid`) — Category: `FISH`
- `GRAPE` (`grape`) — Category: `FRUIT` — Peak season: `FALL`
- `GREEN_ONION` (`green_onion`) — Category: `VEGETABLE` — Peak season: `FALL/WINTER/SPRING`
- `JAPANESE_RADISH` (`japanese_radish`) — Category: `VEGETABLE` — Peak season: `WINTER`
- `KIWI` (`kiwi`) — Category: `FRUIT` — Peak season: `WINTER`
- `LETTUCE` (`lettuce`) — Category: `VEGETABLE` — Peak season: `SPRING, FALL`
- `LOTUS_ROOT` (`lotus_root`) — Category: `VEGETABLE` — Peak season: `WINTER`
- `MELON` (`melon`) — Category: `FRUIT` — Peak season: `SUMMER`
- `MISO` (`miso`) — Category: `MATERIAL`
- `NAPPA_CABBAGE` (`nappa_cabbage`) — Category: `VEGETABLE` — Peak season: `WINTER`
- `ONION` (`onion`) — Category: `VEGETABLE` — Peak season: `SPRING`
- `ORANGE` (`orange`) — Category: `FRUIT` — Peak season: `WINTER`
- `PEACH` (`peach`) — Category: `FRUIT` — Peak season: `SUMMER`
- `PERSIMMON` (`persimmon`) — Category: `FRUIT` — Peak season: `FALL`
- `PIKE_CONGER` (`pike_conger`) — Category: `FISH` — Peak season: `SUMMER (mid/late)`
- `RICE` (`rice`) — Category: `MATERIAL` — Peak season: `FALL`
- `SALMON_ROE` (`salmon_roe`) — Category: `FISH` — Peak season: `FALL (mid/late)`
- `SARDINE` (`sardine`) — Category: `FISH` — Peak season: `SUMMER (early/mid)`
- `SKIPJACK_TUNA` (`skipjack_tuna`) — Category: `FISH` — Peak season: `SPRING (early/mid), FALL (early/mid)`
- `SOYBEANS` (`soybeans`) — Category: `VEGETABLE`
- `SPINACH` (`spinach`) — Category: `VEGETABLE` — Peak season: `WINTER`
- `STRAWBERRY` (`strawberry`) — Category: `FRUIT` — Peak season: `SPRING`
- `SWEET_POTATO` (`sweet_potato`) — Category: `VEGETABLE` — Peak season: `FALL`
- `TOMATO` (`tomato`) — Category: `VEGETABLE` — Peak season: `SUMMER`

**Other (Prepared, Tools, Drinks, Desserts)**
- `BAMBOO_SHOOT_RICE` (`bamboo_shoot_rice`) — Category: `FOOD`
- `COFFEE` (`coffee`) — Category: `DRINK`
- `COTTON_CANDY` (`cotton_candy`) — Category: `DESSERT`
- `CURRY_RICE` (`curry_rice`) — Category: `FOOD`
- `EEL_RICE_BOWL` (`eel_rice_bowl`) — Category: `FOOD`
- `FRIED_HORSE_MACKEREL` (`fried_horse_mackerel`) — Category: `FOOD`
- `GRILLED_AYU` (`grilled_ayu`) — Category: `FOOD`
- `GRILLED_MACKEREL` (`grilled_mackerel`) — Category: `FOOD`
- `GRILLED_PACIFIC_SAURY` (`grilled_pacific_saury`) — Category: `FOOD`
- `GRILLED_SQUID` (`grilled_squid`) — Category: `FOOD`
- `HAMBURG_STEAK` (`hamburg_steak`) — Category: `FOOD`
- `MARINATED_EGGPLANT` (`marinated_eggplant`) — Category: `FOOD`
- `MISO_MACKEREL` (`miso_mackerel`) — Category: `FOOD`
- `MISO_SOUP` (`miso_soup`) — Category: `FOOD`
- `NIKUJAGA` (`nikujaga`) — Category: `FOOD`
- `ODEN` (`oden`) — Category: `FOOD`
- `PAPER_FAN` (`paper_fan`) — Category: `TOOL`
- `PARFAIT` (`parfait`) — Category: `DESSERT`
- `PIKE_CONGER` included above as an ingredient; prepared items below continue:
- `SALAD` (`salad`) — Category: `FOOD`
- `SALMON_ROE_SUSHI` (`salmon_roe_sushi`) — Category: `FOOD`
- `SAUSAGE` (`sausage`) — Category: `FOOD`
- `SEA_URCHIN_SUSHI` (`sea_urchin_sushi`) — Category: `FOOD`
- `SEAFOOD_RICE_FOWL` (`seafood_rice_fowl`) — Category: `FOOD`
- `SHAVED_ICE` (`shaved_ice`) — Category: `DESSERT`
- `SOBA` (`soba`) — Category: `FOOD`
- `SOFT_SERVE_ICE_CREAM` (`soft_serve_ice_cream`) — Category: `DESSERT`
- `SQUID_SUSHI` (`squid_sushi`) — Category: `FOOD`
- `STEAMED_RICE` (`steamed_rice`) — Category: `FOOD`
- `SAUSAGE` (`sausage`) — Category: `FOOD`
- `TAKOYAKI` (`takoyaki`) — Category: `FOOD`
- `TAMAGO_SUSHI` (`tamago_sushi`) — Category: `FOOD`
- `TERIYAKI_YELLOWTAIL` (`teriyaki_yellowtail`) — Category: `FOOD`
- `TONKATSU` (`tonkatsu`) — Category: `FOOD`
- `TUNA_SUSHI` (`tuna_sushi`) — Category: `FOOD`
- `UDON` (`udon`) — Category: `FOOD`
- `YAKISOBA` (`yakisoba`) — Category: `FOOD`

Notes:
- "Ingredients" group includes items categorized as `VEGETABLE`, `FRUIT`, `FISH`, or `MATERIAL` in the registry entry — these are typically raw components for recipes.
- "Other" contains prepared dishes, desserts, drinks, and tools (categories like `FOOD`, `DESSERT`, `DRINK`, `TOOL`).
- Use this as a quick reference when designing recipes or ingredient lists. For full definitions and food values, see `plugins/plugin-cooking/src/main/kotlin/net/azisaba/vanilife/cooking/CookingItems.kt`.
