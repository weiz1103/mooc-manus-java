# AgentFlow Java 鏋舵瀯鎵嬪唽

> 杩欎唤鏂囨。鍙湁涓€涓洰鏍囷細鎶婂綋鍓嶉」鐩娓呮銆?
>
> 閫傚悎 3 绫诲満鏅細
> 1. 鏂板悓瀛﹀揩閫熺悊瑙ｉ」鐩紱
> 2. 瀵瑰浠嬬粛椤圭洰鏋舵瀯锛?
> 3. 闈㈣瘯閲岃椤圭洰鏃舵湁涓€濂楃ǔ瀹氳娉曘€?

---

## 1. 鍏堢敤涓€鍙ヨ瘽璁茶繖涓」鐩?

`agentflow-java` 鏄竴涓熀浜?**Java 21 + Spring Boot + Spring AI + Redis + PostgreSQL** 鐨?Agent 鍚庣绯荤粺銆?

瀹冧笉鏄€滆皟涓€涓嬫ā鍨嬫帴鍙ｂ€濈殑 Demo锛岃€屾槸涓€涓湡姝ｆ寜鍚庣宸ョ▼鏂瑰紡缁勭粐璧锋潵鐨勭郴缁燂細

- 鍓嶇閫氳繃 **SSE** 鐪嬫墽琛岃繃绋嬶紱
- 鍚庣鎶婁竴娆＄敤鎴疯緭鍏ュ彉鎴愪竴涓?**鍚庡彴浠诲姟**锛?
- Worker 鐪熸鎵ц Agent锛?
- 杩囩▼浜嬩欢鍚屾椂鍐欏叆 **浼氳瘽鍘嗗彶**銆?*浠诲姟浜嬩欢璐︽湰** 鍜?**Redis Stream**锛?
- 鏀寔 **鏂嚎缁祦**銆?*鍋滄浠诲姟**銆?*寰幆璇嗗埆**銆?*绂荤嚎鍥炴斁** 鍜?**鎵ц璇勪及**銆?

---

## 2. 杩欏绯荤粺涓昏瑙ｅ喅浠€涔堥棶棰?

濡傛灉鍙仛涓€涓畝鍗曡亰澶╂帴鍙ｏ紝鍏跺疄 Controller 璋冧竴涓嬪ぇ妯″瀷灏卞浜嗐€?

浣?Agent 鍦烘櫙涓嶄竴鏍凤紝瀹冮€氬父浼氶亣鍒拌繖浜涢棶棰橈細

- 涓€娆′换鍔″彲鑳芥墽琛屽緢涔咃紱
- 涓棿浼氭湁寰堝姝ラ鍜屽伐鍏疯皟鐢紱
- 鍓嶇甯屾湜瀹炴椂鐪嬪埌杩囩▼锛?
- 鐢ㄦ埛鏂嚎鍚庤繕鎯崇户缁湅锛?
- 浠诲姟鍙兘澶辫触銆佺瓑寰呯敤鎴疯緭鍏ワ紝鎴栬€呰鎵嬪姩鍋滄锛?
- 涓婄嚎鍚庤繕瑕佽兘鐭ラ亾鈥滆繖涓?Agent 鍒板簳璺戝緱濂戒笉濂解€濄€?

鎵€浠ヨ繖涓」鐩殑閲嶇偣涓嶆槸鈥滆妯″瀷鍥炵瓟闂鈥濓紝鑰屾槸鎶?**妯″瀷鑳藉姏銆佷换鍔¤皟搴︺€佷簨浠舵祦銆佺姸鎬佹満銆佸彲鎭㈠鎬с€佸彲瑙傛祴鎬?* 鏀捐繘涓€涓竻鏅扮殑鍚庣鏋舵瀯閲屻€?

---

## 3. 褰撳墠妯″潡鎬庝箞鍒嗗伐

### 3.1 `agentflow-api`

涓诲簲鐢ㄦ湇鍔★紝璐熻矗锛?

- REST / SSE 鎺ュ彛
- Session 绠＄悊
- 鑱婂ぉ璇锋眰鎺ュ叆
- 浠诲姟鎻愪氦涓庡仠姝?
- Worker 璋冨害
- 浜嬩欢鎸佷箙鍖?
- 瑙傛祴涓庤瘎浼版帴鍙?

### 3.2 `agentflow-spring-ai`

Agent 杩愯鍐呮牳锛岃礋璐ｏ細

- Planner Agent
- ReAct Agent
- Planner + ReAct Flow
- Tool callback registry
- Memory / session state 鎶借薄

### 3.3 `agentflow-common`

鍏叡濂戠害灞傦紝璐熻矗锛?

- `BaseEvent` 鍙婂悇绉?SSE 浜嬩欢妯″瀷
- DTO
- 閫氱敤 `ToolResult`

### 3.4 `agentflow-sandbox-server`

渚ц竟鎵ц鐜锛岃礋璐ｏ細

- Shell
- 鏂囦欢璇诲啓
- 娴忚鍣ㄨ兘鍔?
- 娌欑鍐呮墽琛岀幆澧?

---

## 4. 涓€鏉′富绾匡細涓€娆¤亰澶╄姹傛槸鎬庝箞璺戝畬鐨?

杩欐槸鏁翠釜绯荤粺鏈€閲嶈鐨勪竴鏉′富绾匡細

```text
鍓嶇鍙戦€佹秷鎭?
-> SessionController
-> ChatService
-> TaskDispatchQueue.submit(...)
-> AgentTaskWorker
-> AgentRunner
-> SpringAIPlannerReActFlow
-> AgentEventBus
-> EventPersister / SessionStateSync / SSE
```

鎶婂畠灞曞紑鍚庯紝灏辨槸涓嬮潰杩欐潯鏇村畬鏁寸殑閾捐矾銆?

### 绗?1 姝ワ細鍓嶇鍙戣捣鑱婂ぉ璇锋眰

鎺ュ彛浠嶇劧淇濇寔鍏煎锛?

- `/api/sessions/{sessionId}/chat`
- `/api/v1/sessions/{sessionId}/chat`

杩斿洖鍊间粛鐒舵槸 `text/event-stream`銆?

杩欑偣娌℃湁鍙橈紝鍥犱负鍓嶇浜や簰璇箟鏄喕缁撶殑銆?

### 绗?2 姝ワ細`ChatService` 鎺ヤ綇璇锋眰

`ChatService` 鍙仛涓ょ被浜嬶細

1. **鍙戦€佹柊娑堟伅**
2. **鏂嚎缁祦**

鍙戦€佹柊娑堟伅鏃讹紝瀹冧細鍏堝仛鍑犱欢鍓嶇疆鍔ㄤ綔锛?

- 鏍￠獙浼氳瘽鏄惁瀛樺湪锛?
- 鏍￠獙褰撳墠浼氳瘽鏄笉鏄凡缁忓湪杩愯锛?
- 鍋氬噯鍏ユ帶鍒讹細棰戠巼闄愬埗銆佹椿鍔ㄤ换鍔￠檺鍒躲€?4 灏忔椂閰嶉锛?
- 璁板綍鐢ㄦ埛娑堟伅锛?
- 涓烘湰杞璇濆噯澶?`Task` 鍜?`Sandbox`锛?
- 鎶婄敤鎴锋秷鎭厛鍥炴樉鎴?`message` 浜嬩欢銆?

### 绗?3 姝ワ細鎶婅姹傚彉鎴愬悗鍙颁换鍔?

鐜板湪涓嶆槸 HTTP 绾跨▼鐩存帴璺戝畬鏁翠釜 Agent 浜嗐€?

`ChatService` 浼氭妸杩欐鎵ц鎻愪氦鎴愪竴涓?`AgentTaskCommand`锛屽啓杩?Redis Stream 鍒嗗彂闃熷垪銆?

鍚屾椂锛屼細鍒涘缓鎴栨洿鏂颁竴鏉?`TaskExecution` 璁板綍锛岃〃绀鸿繖娆′换鍔″凡缁忚繘鍏ョ郴缁熴€?

### 绗?4 姝ワ細`AgentTaskWorker` 鍚庡彴娑堣垂浠诲姟

Worker 浼氫粠 Redis Stream consumer group 涓媺浠诲姟銆?

鐜板湪杩欐潯閾捐矾宸茬粡鍏峰涓嬮潰杩欎簺浼佷笟绾х壒寰侊細

- consumer group 娑堣垂
- pending list
- stale claim
- ack + delete
- worker 閲嶅惎鍚庡彲鎺ョ鏈畬鎴愭秷鎭?

杩欐剰鍛崇潃绯荤粺涓嶅啀鏄€滃綋鍓嶈繘绋嬫椿鐫€灏辫兘璺戙€佹寕浜嗗氨涓⑩€濄€?

### 绗?5 姝ワ細`AgentRunner` 鐪熸椹卞姩 Agent 鎵ц

`AgentRunner` 璐熻矗锛?

- 璋冪敤缁熶竴 `AgentStrategy`
- 娑堣垂 Agent 浜х敓鐨勪簨浠舵祦
- 鎶婁簨浠跺啓杩?task output stream
- 鍋氬彇娑堟鏌?
- 鎶婁簨浠朵氦缁欎簨浠舵€荤嚎

濡傛灉鎵ц鍑洪敊锛屽畠浼氳ˉ `error` 浜嬩欢锛?
濡傛灉浠诲姟琚彇娑堬紝瀹冧細琛?`done` 浜嬩欢锛屼繚鎸佸墠绔粓姝㈣涔夊吋瀹广€?

### 绗?6 姝ワ細`SpringAIPlannerReActFlow` 璐熻矗 AI 鎵ц杩囩▼

杩欎竴灞傛槸 Agent 杩愯鍐呮牳銆?

鏍稿績鎬濊矾鏄細

- 鍏堢敱 Planner 鐢熸垚璁″垝锛?
- 鍐嶇敱 ReAct 鎸夋楠ゆ墽琛岋紱
- 鎵ц杩囩▼涓彲浠ヨ皟鐢ㄥ伐鍏凤紱
- 鏍规嵁宸ュ叿缁撴灉鏇存柊鍚庣画姝ラ锛?
- 鏈€鍚庢€荤粨杈撳嚭銆?

鎵€浠ュ畠涓嶆槸涓€杞€滄ā鍨嬮棶绛斺€濓紝鑰屾槸涓€涓湁鐘舵€佺殑澶氭娴佺▼銆?

### 绗?7 姝ワ細`AgentEventBus` 缁熶竴澶勭悊浜嬩欢鍓綔鐢?

鐜板湪姣忎釜浜嬩欢浼氭寜鍥哄畾椤哄簭璧?3 浠朵簨锛?

1. **鍏堟寔涔呭寲**
   - 浼氳瘽鍘嗗彶
   - `task_event_log`
2. **鍐嶅悓姝ヤ細璇濇姇褰?*
   - 鏍囬
   - 鐘舵€?
   - 鏈鏁?
   - 鏈€鏂版秷鎭?
3. **鏈€鍚庢帹 SSE**

杩欒绯荤粺閲屸€滀簨浠惰惤鍝噷銆佺姸鎬佹€庝箞鏀广€佸墠绔綍鏃剁湅鍒扳€濋兘璧板悓涓€鏉¤矾锛屼笉鍐嶆暎鍦ㄥ涓被閲屻€?

---

## 5. 澶氭潯杈呯嚎锛氫富绾夸箣澶栬繕鏈夊摢浜涢噸瑕佽兘鍔?

### 5.1 杈呯嚎 A锛氫换鍔¤皟搴︾嚎

鏍稿績瀵硅薄锛?

- `TaskDispatchQueue`
- `AgentTaskWorker`
- `TaskExecution`

鑱岃矗锛?

- 鎻愪氦浠诲姟
- 鍚庡彴娑堣垂
- worker 鎺ョ
- lease / heartbeat
- stop / cancel

### 5.2 杈呯嚎 B锛氫簨浠惰处鏈笌鍥炴斁绾?

鏍稿績瀵硅薄锛?

- `task_event_log`
- `TaskEvaluationService`
- task output stream

鑱岃矗锛?

- 璁板綍浠诲姟瀹屾暣浜嬩欢杩囩▼
- 鐢ㄤ簬 replay
- 鐢ㄤ簬绂荤嚎鍒嗘瀽
- 鐢ㄤ簬闂鎺掓煡

杩欓噷瑕佺壒鍒尯鍒嗕袱涓蹇碉細

#### `task_executions`

杩欐槸浠诲姟鐨?*褰撳墠鎬佸揩鐓?*锛屾瘮濡傦細

- 褰撳墠鐘舵€?
- 鏄惁 loop
- tool 璋冪敤娆℃暟
- 鏈€鍚庝竴涓簨浠?id
- 杩愯鏃跺憡璀?

#### `task_event_log`

杩欐槸浠诲姟鐨?*杩囩▼璐︽湰**锛屾寜浜嬩欢涓€鏉℃潯杩藉姞锛岀敤鏉ュ仛锛?

- 瀹屾暣鍥炴斁
- 瀹¤
- 绂荤嚎璇勪及
- 澶嶇洏

涓€鍙ヨ瘽鐞嗚В锛?

- `task_executions` 鐪嬧€滅幇鍦ㄦ€庢牱鈥濓紱
- `task_event_log` 鐪嬧€滆繃绋嬪彂鐢熶簡浠€涔堚€濄€?

### 5.3 杈呯嚎 C锛氳娴嬭瘎浼扮嚎

鏍稿績瀵硅薄锛?

- `ExecutionObservationSink`
- `InMemoryExecutionObservationSink`
- `TaskEvaluationService`

鑱岃矗锛?

- 缁熻浜嬩欢鏁?
- 缁熻 tool 璋冪敤娆℃暟
- 璇嗗埆閲嶅宸ュ叿璋冪敤
- 鏍囪 loop 椋庨櫓
- 缁欎换鍔℃墦鍒?
- 杈撳嚭 loop report

### 5.4 杈呯嚎 D锛氭矙绠辫兘鍔涚嚎

鏍稿績瀵硅薄锛?

- `Sandbox`
- `Browser`
- `agentflow-sandbox-server`

鑱岃矗锛?

- 鎵ц shell
- 鏂囦欢璇诲啓
- 娴忚鍣拷锟戒綔
- 缁?Agent 鎻愪緵瀹夊叏鍙帶鐨勫閮ㄦ墽琛岀幆澧?

---

## 6. 褰撳墠鏈€鍏抽敭鐨?4 涓牳蹇冩ā鍨?

### 6.1 `Session`

杩欐槸鐢ㄦ埛鐪嬪埌鐨勪細璇濄€?

瀹冪殑瀵瑰鐘舵€佷繚鎸佺畝鍗曪細

- `pending`
- `running`
- `waiting`
- `completed`

杩欐牱鍋氱殑濂藉鏄墠绔涔夌ǔ瀹氥€?

### 6.2 `Task`

杩欐槸涓€娆?Agent 鎵ц瀵瑰簲鐨勮繍琛岄€氶亾銆?

瀹冭儗鍚庝富瑕佹寕鐫€涓ゆ潯 Redis Stream锛?

- input / dispatch 渚?
- output / replay 渚?

### 6.3 `TaskExecution`

杩欐槸鍚庡彴浠诲姟鐨勬墽琛屽揩鐓с€?

鍐呴儴鐘舵€佹洿缁嗭細

- `SUBMITTED`
- `DISPATCHED`
- `RUNNING`
- `WAITING`
- `COMPLETED`
- `FAILED`
- `CANCEL_REQUESTED`
- `CANCELLED`

杩欎釜鐘舵€佹満涓昏鏈嶅姟浜庡悗绔不鐞嗭紝涓嶇洿鎺ユ毚闇茬粰鍓嶇銆?

### 6.4 `BaseEvent`

杩欐槸鏁翠釜绯荤粺閲屾渶绋冲畾鐨勫崗璁ā鍨嬨€?

鍓嶇鑳界湅鍒扮殑 SSE 浜嬩欢浠嶇劧鏄細

- `message`
- `plan`
- `title`
- `step`
- `tool`
- `wait`
- `error`
- `done`

杩欏浜嬩欢濂戠害鐜板湪鏄富杈圭晫锛屽悗绔唴閮ㄦ€庝箞鏀癸紝閮戒笉鑳介殢渚挎敼瀹冦€?

---

## 7. 褰撳墠鍙潬鎬ф槸鎬庝箞鍋氱殑

### 7.1 涓轰粈涔堝墠绔柇绾垮悗杩樿兘缁х画鐪?

鍥犱负浜嬩欢鍦ㄦ帹缁欏墠绔墠锛屽凡缁忓厛鍐欒繘锛?

- Redis task output stream
- 鏁版嵁搴撲細璇濆巻鍙?
- `task_event_log`

鎵€浠ュ墠绔柇绾块噸杩炴椂锛屽彲浠ュ甫鐫€ `event_id` 缁х画璇伙紝涓嶉渶瑕佹暣杞噸璺戙€?

### 7.2 涓轰粈涔?worker 鎸備簡浠诲姟杩樿兘鎺ュ洖鏉?

鍥犱负 Redis Stream dispatch queue 涓嶆槸绠€鍗?list锛岃€屾槸 consumer group 妯″紡銆?

宸茬粡鍙栬蛋浣嗘病 ack 鐨勬秷鎭細杩涘叆 pending銆?
鍏朵粬 worker 鍙互鎸?idle time claim 杩欎簺娑堟伅銆?

### 7.3 stop 涓轰粈涔堢幇鍦ㄦ洿缁熶竴

鐜板湪 stop 涓嶅啀鍙槸鈥滅洿鎺ユ敼 Session 鐘舵€佲€濄€?

鑰屾槸锛?

1. 鍏堝啓鍏?cancel intent锛?
2. 杩愯涓殑浠诲姟鍦ㄦ墽琛屽惊鐜噷鍋氬崗浣滃紡鍙栨秷妫€鏌ワ紱
3. 濡傛灉浠诲姟杩樻病鍚姩锛屼細鐩存帴琛ヤ竴涓?`done`锛?
4. 鍐呴儴鐘舵€佺粺涓€钀藉埌 `TaskExecution`銆?

### 7.4 涓轰粈涔?replay 鏇撮潬璋变簡

鐜板湪 replay 鐨勪富鏉ユ簮鏄?`task_event_log`銆?

`task_executions.replay_events` 杩樹繚鐣欙紝浣嗗畾浣嶅凡缁忛檷鎴愮儹缂撳瓨鍜屽吋瀹瑰厹搴曪紝涓嶅啀鎵挎媴涓昏处鏈亴璐ｃ€?

---

## 8. 褰撳墠鍑嗗叆鎺у埗鏄€庝箞鍋氱殑

鐜板湪宸茬粡琛ヤ簡 3 绫诲熀纭€鍑嗗叆瑙勫垯锛屽叏閮ㄦ斁鍦ㄨ亰澶╂彁浜ゅ叆鍙ｏ細

1. **姣忓垎閽熸秷鎭鐜囬檺鍒?*
   - Redis 鍙敤鏃惰蛋 Redis 璁℃暟绐楀彛
   - Redis 涓嶅彲鐢ㄦ椂閫€鍥炶繘绋嬪唴鍏滃簳
2. **鍗曚細璇濇椿鍔ㄤ换鍔￠檺鍒?*
   - 榛樿涓€涓細璇濆悓涓€鏃堕棿鍙厑璁镐竴涓椿鍔ㄤ换鍔?
3. **24 灏忔椂浠诲姟閰嶉**
   - 鐩存帴浠?`task_executions` 缁熻

閲嶈鐨勬槸锛?

- 鍓嶇鎺ュ彛娌″彉锛?
- 琚嫆缁濇椂浠嶇劧璧?SSE锛?
- 琛ㄧ幇褰㈠紡浠嶇劧鏄?`error` + `done`銆?

鎵€浠ョ敤鎴蜂綋楠屼笉浼氱獊鐒舵柇鎺夈€?

---

## 9. 褰撳墠瑙傛祴涓庤瘎浼拌兘鍋氬埌浠€涔堢▼搴?

鐩墠宸茬粡涓嶆槸鈥滃彧鑳界湅鏃ュ織鈥濅簡锛岃€屾槸鏈夊熀纭€闂幆锛?

### 鍦ㄧ嚎鍙湅

- 浠诲姟鐘舵€?
- event 鏁?
- tool 璋冪敤鏁?
- step 鏁?
- loop 椋庨櫓
- warnings

### 绂荤嚎鍙湅

- `replay`
- `scorecard`
- `loop report`

涔熷氨鏄锛岀幇鍦ㄥ凡缁忚兘鍥炵瓟涓嬮潰杩欎簺闂锛?

- 杩欐浠诲姟鎵ц鍒板摢涓€姝ヤ簡锛?
- 鏄惁閲嶅璋冪敤浜嗗悓涓€涓伐鍏凤紵
- 鏈€缁堟槸姝ｅ父瀹屾垚銆佸け璐ャ€佺瓑寰咃紝杩樻槸鍙栨秷锛?
- 杩欐鎵ц璐ㄩ噺澶ф濡備綍锛?

---

## 10. 褰撳墠鍖呯粨鏋勬€庝箞鐞嗚В鏈€椤?

寤鸿鎸変笅闈㈤『搴忚浠ｇ爜锛?

1. `SessionController`
2. `ChatService`
3. `AgentTaskWorker`
4. `AgentRunner`
5. `SpringAIPlannerReActFlow`
6. `AgentEventBus`
7. `EventPersister`
8. `TaskExecutionService`
9. `TaskEvaluationService`

濡傛灉鎸夎繖鏉￠『搴忕湅锛岄」鐩富绾夸細闈炲父娓呮銆?

### `agentflow-api` 褰撳墠鎺ㄨ崘鐞嗚В鏂瑰紡

```text
interfaces      瀵瑰鎺ュ彛
service         搴旂敤缂栨帓
agent           鎵ц鍏ュ彛涓庣瓥鐣?
event           浜嬩欢鍓綔鐢ㄧ閬?
domain          妯″瀷 / 浠撳偍鎺ュ彛 / 澶栭儴绔彛
infrastructure  JPA / Redis / Sandbox / SpringAI 閫傞厤
observability   瑙傛祴涓庤瘎浼?
```

鐜板湪杩欏缁撴瀯宸茬粡姣斾箣鍓嶆竻妤氬緢澶氥€?

鍚庨潰濡傛灉缁х画鏀舵暃锛屼富瑕佹槸鎶?`service` 閲屼竴閮ㄥ垎搴旂敤缂栨帓鍐嶅線鏇存槑纭殑 application 缁撴瀯杩侊紝浣嗚繖涓€姝ヤ笉鎬ワ紝鍏堜繚璇佷富绾跨ǔ瀹氭洿閲嶈銆?

---

## 11. 杩欎釜椤圭洰鏈€閫傚悎鎬庝箞瀵瑰璁?

### 11.1 30 绉掔増鏈?

> 杩欐槸涓€涓?Java 鐗堢殑 Agent 鍚庣绯荤粺銆傚墠绔€氳繃 SSE 鐪嬭繃绋嬶紝鍚庣鎶婁竴娆＄敤鎴疯緭鍏ユ彁浜ゆ垚鍚庡彴浠诲姟锛岀敱 Worker 鐪熸鎵ц Planner + ReAct Agent銆傛墽琛岃繃绋嬩腑鐨勪簨浠朵細鍚屾椂鍐欏叆 Redis Stream銆佷細璇濆巻鍙插拰浠诲姟浜嬩欢璐︽湰锛屾墍浠ョ郴缁熸敮鎸佹柇绾跨画娴併€佷换鍔″洖鏀俱€佸仠姝㈠彇娑堝拰鎵ц璇勪及銆?

### 11.2 3 鍒嗛挓鐗堟湰

> 杩欎釜椤圭洰鏈€鏍稿績鐨勭偣锛屼笉鏄帴浜?Spring AI锛岃€屾槸鎶?Agent 鍋氭垚浜嗕竴涓湡姝ｅ彲杩愯銆佸彲鎭㈠銆佸彲瑙傚療鐨勫悗绔郴缁熴€傜敤鎴峰彂娑堟伅鍚庯紝璇锋眰涓嶄細鐩存帴鍦?HTTP 绾跨▼閲屾妸鏁磋疆 Agent 璺戝畬锛岃€屾槸鍏堣繘鍏ヤ换鍔″垎鍙戦槦鍒楋紝鐢卞悗鍙?worker 鍘绘秷璐广€侫gent 鎵ц杩囩▼浼氫笉鏂骇鍑?`message / plan / step / tool / wait / done` 杩欎簺浜嬩欢銆備簨浠跺厛钀藉簱銆佸啀鏇存柊浼氳瘽鐘舵€併€佹渶鍚庢帹缁欏墠绔紝鍚屾椂 Redis Stream 杩樹繚瀛樹簡缁祦鎵€闇€鐨?event id銆傝繖鏍峰氨鎶婇暱浠诲姟銆佹柇绾跨画娴併€佸け璐ユ仮澶嶃€佸仠姝㈠彇娑堝拰绂荤嚎璇勪及涓叉垚浜嗕竴鏉℃竻鏅颁富绾裤€?

### 11.3 闈㈣瘯閲屾渶鍊煎緱璁茬殑浜偣

寤鸿閲嶇偣璁茶繖 5 涓細

1. **鍓嶇璇箟鍐荤粨锛屽悗绔笎杩涢噸鏋?*
2. **鍚屾鎵ц鏀归€犳垚鍚庡彴浠诲姟 + worker 娑堣垂**
3. **`TaskExecution` + `task_event_log` 鍙屽眰妯″瀷**
4. **Redis Stream consumer group + pending + claim**
5. **loop detection + replay + scorecard**

---

## 12. 褰撳墠绯荤粺宸茬粡鍋氬埌浠€涔堢▼搴?

### 宸茬粡瀹屾垚鐨?

- 鍚庡彴浠诲姟鍖栨墽琛?
- Worker recoverable 娑堣垂
- 缁熶竴 stop / cancel 璇箟
- `TaskExecution` 鐘舵€佹満
- `task_event_log` 浜嬩欢璐︽湰
- replay / score / loop report
- 浼氳瘽绾ч檺娴?/ 娲诲姩浠诲姟闄愬埗 / 閰嶉
- 鏂囨。涓庝富绾挎敹鏁?

### 杩樺€煎緱缁х画鍋氱殑

- 鏇翠弗鏍肩殑 worker watchdog / lease 鎺ョ
- 鏇存繁灞傜殑宸ュ叿鎵ц鍙栨秷浼犳挱
- Micrometer / OpenTelemetry
- 鐢ㄦ埛 / 绉熸埛绾ч厤棰?
- 姝讳俊闃熷垪 / poison message 娌荤悊
- 鏇村畬鏁寸殑绂荤嚎璇勬祴闆?

---

## 13. 鏈€鍚庣粰涓€涓渶绠€鍗曠殑鐞嗚В鍥?

```text
涓€鏉′富绾匡細
鍓嶇璇锋眰 -> ChatService -> 鎻愪氦浠诲姟 -> Worker 鎵ц -> EventBus -> 钀藉簱/鎶曞奖/SSE

澶氭潯杈呯嚎锛?
- 璋冨害绾匡細dispatch queue / worker / cancel / claim
- 璐︽湰绾匡細task_event_log / replay
- 瑙傛祴绾匡細snapshot / score / loop report
- 娌欑绾匡細shell / file / browser
```

濡傛灉鎶婅繖寮犲浘璁叉竻妤氾紝杩欎釜椤圭洰灏卞凡缁忚兘璁插緱寰堝畬鏁翠簡銆?



