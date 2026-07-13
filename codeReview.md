# Code review

嚴重問題
專案無法編譯
[ProductService.java (line 26)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/ProductService.java:26) 使用 request.getName()/getPrice()/getStock()，但 [ProductRequest.java (line 3)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/dto/ProductRequest.java:3) 是 Java record，accessor 應為 name()/price()/stock()。
後果：mvn test 直接失敗，服務無法交付。

商品管理 API 沒有 ADMIN 權限控管
[SecurityConfig.java (line 24)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/config/SecurityConfig.java:24) 只設定 .anyRequest().authenticated()，註解說商品新增/修改/刪除限 ADMIN，但實際所有登入使用者都可操作。
後果：一般 USER 可上架、修改、刪除商品。

JWT filter 沒有角色權限，且可能沒有註冊成 Bean
[JwtAuthenticationFilter.java (line 14)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/security/JwtAuthenticationFilter.java:14) 沒有 @Component 或 @Bean 註冊；即使註冊成功，第 31 行 authorities 是空集合。
後果：Spring 啟動可能失敗；也無法做 hasRole("ADMIN")。

密碼明文儲存與明文比對
[AuthService.java (line 23)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/AuthService.java:23) 註解說 BCrypt，但第 24 行實際是 equals 明文比對；[data.sql (line 1)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/resources/data.sql:1) 也存明文密碼。
後果：資料庫外洩時帳號立即失守。

JWT secret 硬編碼，且 token 沒有過期時間
[JwtUtil.java (line 14)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/security/JwtUtil.java:14) 使用 hardcoded secret，且第 19-23 行沒有設定 expiration，與註解不符。
後果：token 長期有效，secret 洩漏後無法安全輪替。

下單庫存扣減沒有可靠併發保護
[OrderService.java (line 36)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/OrderService.java:36) placeOrder 沒有 @Transactional；第 62 行 @Transactional 在同 class 內自呼叫通常不會經過 Spring proxy；也沒有 DB lock 或 @Version。
後果：併發下單可能超賣、訂單與庫存狀態不一致。

下單驗證永遠通過
[OrderService.java (line 68)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/OrderService.java:68) validateOrder() 永遠回傳 true。
後果：quantity <= 0、productId == null 等資料可進入流程；負數數量甚至可能增加庫存。

搜尋 JPQL injection
[ProductService.java (line 55)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/ProductService.java:55) 直接拼接 keyword，註解卻說已參數化。
後果：使用者輸入可破壞查詢語意，造成資料外洩或查詢失敗。

🟡 中等問題
[ProductController.java (line 30)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/controller/ProductController.java:30) 用 GET /delete/{id} 做刪除，不符合 HTTP 語意，也容易被誤觸。
[ProductRepository.java (line 10)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/repository/ProductRepository.java:10) findByCategory 對應不到 Product.category，後續修完編譯後可能造成 Spring Data 啟動錯誤。
[OrderService.java (line 58)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/OrderService.java:58) (int) product.getPrice() 會截斷小數，金額計算錯誤。
Entity 和 SQL 缺少 NOT NULL、FK、unique constraint，例如 [schema.sql (line 9)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/resources/schema.sql:9) users.username 沒 unique，導致 [data.sql (line 1)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/resources/data.sql:1) 的 ON CONFLICT DO NOTHING 沒有可靠衝突目標。
[ProductService.java (line 44)](/Users/yanchengzhuo/Document/Nova/project0708/starter-project/src/main/java/com/trading/platform/service/ProductService.java:44) findAll() 無分頁，資料量大時會拖垮 API。
Controller 直接回傳 Entity，容易暴露內部欄位與造成 lazy loading/序列化問題。
🟢 輕微問題 / 可改善
多處用 System.out.println，應改用 logger。
SimpleDateFormat 與 placedOrderCount 是 shared mutable state；目前 orderNo 也沒有存入 DB，價值有限。
AuthController 登入失敗回 200 + 字串 "登入失敗"，API 語意不清楚，應回 401 與一致格式。
