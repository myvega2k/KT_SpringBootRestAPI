### Lecture API
* Token Payload 포함되는 subject를 email에서 userId(UUID값)으로 변경함
  ![JWT Token](/img/token1.png "Payload의 subject")

* Dynamic Permission Example   
  [Spring Boot 3.0 Security with JWT](https://github.com/ali-bouali/spring-boot-3-jwt-security)

* Swagger API docs에 Authorization 추가

![Swagger Authorize](/img/swagger_auth_01.png "Swagger Authorize1")
![Swagger Authorize](/img/swagger_auth_02.png "Swagger Authorize1")

* Authorization value에 Token을 입력하고 Authorize 버튼을 클릭하면 토큰인증이 된다.
* SpringDocsConfig 클래스에 Swagger Authorize 설정 되었습니다.