# AgentFlow Java

`agentflow-java` 鏄竴涓潰鍚?Agent 鍦烘櫙鐨?Java 鍚庣椤圭洰銆傚畠鍋氱殑浜嬫儏寰堢洿鎺ワ細**鎶婁竴娆＄敤鎴疯緭鍏ュ彉鎴愬彲寮傛鎵ц銆佸彲瀹炴椂鏌ョ湅銆佸彲涓柇鎭㈠銆佸彲鍥炴斁璇勪及鐨?Agent 浠诲姟銆?*

瀹冧笉鏄竴涓彧婕旂ず妯″瀷璋冪敤鐨?Demo锛岃€屾槸涓€濂楁寜鍚庣宸ョ▼鏂瑰紡缁勭粐璧锋潵鐨?Agent 绯荤粺銆?

---

## 椤圭洰绠€浠?

杩欎釜椤圭洰鍩轰簬 **Java 21 + Spring Boot + Spring AI + Redis + PostgreSQL** 鏋勫缓锛屽墠绔€氳繃 **SSE** 鏌ョ湅鎵ц杩囩▼锛屽悗绔妸璇锋眰鎻愪氦涓哄悗鍙颁换鍔★紝鐢?Worker 寮傛鎵ц Planner + ReAct 娴佺▼锛屽苟鎶婃墽琛屼簨浠跺悓姝ュ啓鍏ヤ細璇濆巻鍙层€佷换鍔¤处鏈拰浜嬩欢娴併€?

瀹冮噸鐐硅В鍐崇殑鏄笅闈㈣繖浜涢棶棰橈細

- 涓€娆?Agent 浠诲姟鍙兘鎵ц寰堜箙锛屼笉鑳戒竴鐩村崰鐫€ HTTP 绾跨▼
- 鍓嶇甯屾湜瀹炴椂鐪嬪埌杩囩▼锛岃€屼笉鏄彧鎷挎渶缁堢粨鏋?
- 鐢ㄦ埛鏂嚎鍚庯紝甯屾湜杩樿兘浠庝笂娆＄殑浣嶇疆缁х画鐪?
- 浠诲姟鍙兘澶辫触銆佺瓑寰呰緭鍏ワ紝鎴栬€呰鎵嬪姩鍋滄
- 鎵ц杩囩▼闇€瑕佽兘鍥炴斁銆佹帓鏌ュ拰璇勪及

---

## 鏍稿績鑳藉姏

褰撳墠绯荤粺宸茬粡鍏峰杩欎簺鏍稿績鑳藉姏锛?

- **SSE 瀹炴椂鎺ㄩ€?*锛氬墠绔彲浠ユ寔缁湅鍒?`message / plan / step / tool / done` 绛変簨浠?
- **鍚庡彴浠诲姟鎵ц**锛氳姹傛彁浜ゅ悗鐢?Worker 寮傛娑堣垂锛屼笉鍦?HTTP 绾跨▼閲岄暱鏃堕棿闃诲
- **鏂嚎缁祦**锛氬鎴风鍙互鍩轰簬 `event_id` 浠庢柇鐐圭户缁鍙栨墽琛岃繃绋?
- **鍋滄涓庡彇娑?*锛氳繍琛屼腑鐨勪换鍔″彲浠ュ崗浣滃紡鍙栨秷
- **浠诲姟鐘舵€佺鐞?*锛氫换鍔℃彁浜ゃ€佸垎鍙戙€佽繍琛屻€佺瓑寰呫€佸畬鎴愩€佸け璐ャ€佸彇娑堥兘鏈夋槑纭姸鎬?
- **浜嬩欢璐︽湰涓庡洖鏀?*锛氭墽琛岃繃绋嬪彲 replay锛屽彲鐢ㄤ簬鎺掓煡鍜屽鐩?
- **鍩虹瑙傛祴涓庤瘎浼?*锛氭敮鎸佹墽琛岃瘎鍒嗐€佸惊鐜闄╄瘑鍒拰 loop report

---

## 妯″潡缁撴瀯

椤圭洰閲囩敤 Maven 澶氭ā鍧楃粨鏋勶細

### `agentflow-api`
涓诲簲鐢ㄦā鍧楋紝璐熻矗锛?

- REST / SSE 鎺ュ彛
- 浼氳瘽绠＄悊
- 鑱婂ぉ涓绘祦绋嬬紪鎺?
- 浠诲姟鎻愪氦涓庡仠姝?
- Worker 璋冨害
- 浜嬩欢鎸佷箙鍖栦笌鐘舵€佸悓姝?
- 鎵ц瑙傛祴涓庤瘎浼版帴鍙?

### `agentflow-spring-ai`
Agent 杩愯鍐呮牳锛岃礋璐ｏ細

- Planner Agent
- ReAct Agent
- Planner + ReAct Flow
- 宸ュ叿鍥炶皟娉ㄥ唽
- Memory / session state 鎶借薄

### `agentflow-common`
鍏叡濂戠害妯″潡锛岃礋璐ｏ細

- `BaseEvent` 鍙婂悇绫讳簨浠舵ā鍨?
- DTO
- 閫氱敤缁撴灉瀵硅薄

### `agentflow-sandbox-server`
娌欑鎵ц妯″潡锛岃礋璐ｏ細

- Shell 鎵ц
- 鏂囦欢璇诲啓
- 娴忚鍣ㄨ兘鍔?
- 鍙楁帶鎵ц鐜

---

## 涓婚摼璺?

涓€娆¤姹傜殑鏍稿績鎵ц閾捐矾濡備笅锛?

```text
鍓嶇鍙戞秷鎭?
-> SessionController
-> ChatService
-> TaskDispatchQueue.submit(...)
-> AgentTaskWorker
-> AgentRunner
-> SpringAIPlannerReActFlow
-> AgentEventBus
-> EventPersister / SessionStateSync / SSE
```

涓€鍙ヨ瘽姒傛嫭杩欐潯閾捐矾锛?

> 鍓嶇鍙戣捣璇锋眰锛屽悗绔妸瀹冨彉鎴愬悗鍙颁换鍔℃墽琛岋紝鎵ц杩囩▼涓寔缁骇鍑轰簨浠讹紝浜嬩欢琚繚瀛樸€佸悓姝ュ苟鎺ㄩ€佺粰鍓嶇銆?

---

## 鍖呯粨鏋?

`agentflow-api` 褰撳墠鎸夎亴璐ｆ媶鍒嗕负涓嬮潰鍑犲眰锛?

```text
agentflow-api
鈹溾攢 interfaces
鈹? 鈹溾攢 rest
鈹? 鈹斺攢 sse
鈹溾攢 application
鈹? 鈹溾攢 service
鈹? 鈹斺攢 agent
鈹溾攢 domain
鈹? 鈹溾攢 model
鈹? 鈹溾攢 repository
鈹? 鈹溾攢 service
鈹? 鈹溾攢 external
鈹? 鈹斺攢 exception
鈹斺攢 infrastructure
   鈹溾攢 event
   鈹溾攢 task
   鈹溾攢 repository
   鈹溾攢 external
   鈹溾攢 observability
   鈹溾攢 springai
   鈹斺攢 config
```

杩欏缁撴瀯鐨勬牳蹇冪洰鐨勫緢绠€鍗曪細

- `interfaces` 璐熻矗瀵瑰鎺ュ彛
- `application` 璐熻矗娴佺▼缂栨帓
- `domain` 鏀炬牳蹇冩ā鍨嬪拰绔彛鎶借薄
- `infrastructure` 鏀炬妧鏈疄鐜板拰澶栭儴閫傞厤

---

## 蹇€熷紑濮?

### 鐜瑕佹眰

- JDK 21+
- Maven 3.9.15+
- PostgreSQL
- Redis

### 缂栬瘧椤圭洰

```powershell
Set-Location "D:\Code\agentflow-master\agentflow\agentflow\agentflow-java"
mvn -q -DskipTests compile
```

### 杩愯娴嬭瘯

```powershell
Set-Location "D:\Code\agentflow-master\agentflow\agentflow\agentflow-java"
mvn -q test
```

### 鍚姩 API 妯″潡

涓诲簲鐢ㄥ叆鍙ｄ綅浜庯細

- `agentflow-api/src/main/java/com/iAGENTFLOW/AgentFlow/api/AgentFlowApiApplication.java`

鍦ㄨˉ榻愭湰鍦伴厤缃悗锛屽彲鐩存帴鍚姩璇ュ簲鐢ㄣ€?

---

## 鏂囨。瀵艰埅

濡傛灉浣犳兂缁х画娣卞叆鐪嬪疄鐜扮粏鑺傦紝寤鸿鎸変笅闈㈤『搴忛槄璇伙細

1. [`docs/README.md`](./docs/README.md)
2. [`docs/ARCHITECTURE_HANDBOOK.md`](./docs/ARCHITECTURE_HANDBOOK.md)
3. [`docs/API_CONTRACT.md`](./docs/API_CONTRACT.md)

---

## 鎬荤粨

AgentFlow Java 鐨勯噸鐐癸紝涓嶆槸鈥滆妯″瀷鍥炵瓟闂鈥濓紝鑰屾槸鎶?**浠诲姟鎵ц銆佷簨浠舵祦銆佺姸鎬佺鐞嗗拰鍓嶇瀹炴椂浜や簰** 杩欏嚑浠朵簨鐪熸涓茶捣鏉ャ€?

濡傛灉浣犳兂蹇€熺湅鎳傝繖涓」鐩紝鍏堜粠鏍?README 鍜屾灦鏋勬枃妗ｅ紑濮嬶紱濡傛灉鍙涓€鍙ヨ瘽锛岄偅灏辨槸锛?*瀹冭В鍐崇殑鏄?Agent 鍦ㄥ悗绔湡姝ｈ惤鍦版椂鐨勬墽琛屻€佺画娴併€佸彇娑堝拰鍥炴斁闂銆?*


