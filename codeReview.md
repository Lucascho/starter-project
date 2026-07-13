# Code Review

以下依問題嚴重程度整理，並附上影響說明。

## 嚴重問題

### 1. 專案無法編譯

- 位置：[ProductService.java](src/main/java/com/trading/platform/service/ProductService.java#L26)、[ProductRequest.java](src/main/java/com/trading/platform/dto/ProductRequest.java#L3)
- 問題：`ProductService` 使用 `request.getName()`、`request.getPrice()`、`request.getStock()`，但 `ProductRequest` 是 Java record，accessor 應為 `name()`、`price()`、`stock()`。
- 影響：`mvn test` 會直接失敗，服務無法交付。

### 2. 商品管理 API 沒有 ADMIN 權限控管

- 位置：[SecurityConfig.java](src/main/java/com/trading/platform/config/SecurityConfig.java#L24)
- 問題：只設定 `.anyRequest().authenticated()`。註解說商品新增、修改、刪除限 ADMIN，但實際上所有登入使用者都可操作。
- 影響：一般 USER 可以上架、修改、刪除商品。

### 3. JWT filter 沒有角色權限，且可能沒有註冊成 Bean

- 位置：[JwtAuthenticationFilter.java](src/main/java/com/trading/platform/security/JwtAuthenticationFilter.java#L14)
- 問題：沒有 `@Component` 或 `@Bean` 註冊；即使註冊成功，authorities 也是空集合。
- 影響：Spring 啟動可能失敗，也無法做 `hasRole("ADMIN")` 權限控管。

### 4. 密碼明文儲存與明文比對

- 位置：[AuthService.java](src/main/java/com/trading/platform/service/AuthService.java#L23)、[data.sql](src/main/resources/data.sql#L1)
- 問題：註解說使用 BCrypt，但實際是用 `equals` 明文比對；`data.sql` 也存明文密碼。
- 影響：資料庫外洩時，帳號會立即失守。

### 5. JWT secret 硬編碼，且 token 沒有過期時間

- 位置：[JwtUtil.java](src/main/java/com/trading/platform/security/JwtUtil.java#L14)
- 問題：使用 hardcoded secret，且沒有設定 expiration，與註解不符。
- 影響：token 會長期有效，secret 洩漏後也不容易安全輪替。

### 6. 下單庫存扣減沒有可靠併發保護

- 位置：[OrderService.java](src/main/java/com/trading/platform/service/OrderService.java#L36)
- 問題：`placeOrder()` 沒有 `@Transactional`；`@Transactional` 放在同 class 內部自呼叫的方法上，通常不會經過 Spring proxy；也沒有 DB lock 或 `@Version`。
- 影響：併發下單時可能超賣，訂單與庫存狀態也可能不一致。

### 7. 下單驗證永遠通過

- 位置：[OrderService.java](src/main/java/com/trading/platform/service/OrderService.java#L68)
- 問題：`validateOrder()` 永遠回傳 `true`。
- 影響：`quantity <= 0`、`productId == null` 等資料可進入流程；負數數量甚至可能增加庫存。

### 8. 搜尋 JPQL injection

- 位置：[ProductService.java](src/main/java/com/trading/platform/service/ProductService.java#L55)
- 問題：直接拼接 `keyword`，但註解卻說已參數化。
- 影響：使用者輸入可破壞查詢語意，造成資料外洩或查詢失敗。

## 中等問題

### 1. 使用 GET 執行刪除

- 位置：[ProductController.java](src/main/java/com/trading/platform/controller/ProductController.java#L30)
- 問題：用 `GET /delete/{id}` 做刪除，不符合 HTTP 語意，也容易被誤觸。

### 2. Repository 方法對應不到 Entity 欄位

- 位置：[ProductRepository.java](src/main/java/com/trading/platform/repository/ProductRepository.java#L10)
- 問題：`findByCategory` 對應不到 `Product.category`。
- 影響：後續修完編譯問題後，可能造成 Spring Data 啟動錯誤。

### 3. 金額計算截斷小數

- 位置：[OrderService.java](src/main/java/com/trading/platform/service/OrderService.java#L58)
- 問題：使用 `(int) product.getPrice()`，會截斷小數。
- 影響：訂單金額計算錯誤。

### 4. Entity 與 SQL 缺少資料約束

- 位置：[schema.sql](src/main/resources/schema.sql#L9)、[data.sql](src/main/resources/data.sql#L1)
- 問題：缺少 `NOT NULL`、FK、unique constraint 等資料庫約束，例如 `users.username` 沒有 unique，導致 `ON CONFLICT DO NOTHING` 沒有可靠衝突目標。
- 影響：資料一致性不足，重複資料或不合法資料可能進入資料庫。

### 5. 商品列表沒有分頁

- 位置：[ProductService.java](src/main/java/com/trading/platform/service/ProductService.java#L44)
- 問題：使用 `findAll()` 無分頁。
- 影響：資料量大時會拖垮 API。

### 6. Controller 直接回傳 Entity

- 問題：Controller 直接回傳 Entity。
- 影響：容易暴露內部欄位，也可能造成 lazy loading 或序列化問題。

## 輕微問題 / 可改善

### 1. 使用 `System.out.println`

- 問題：多處使用 `System.out.println`。
- 建議：改用 logger，方便控制 log level 與輸出格式。

### 2. Shared mutable state

- 問題：`SimpleDateFormat` 與 `placedOrderCount` 是 shared mutable state；目前 `orderNo` 也沒有存入 DB，實際價值有限。
- 建議：移除不必要狀態，或改用 thread-safe 實作。

### 3. 登入失敗回應語意不清楚

- 問題：`AuthController` 登入失敗時回傳 `200` 與字串 `"登入失敗"`。
- 建議：改回 `401 Unauthorized`，並使用一致的錯誤格式。
