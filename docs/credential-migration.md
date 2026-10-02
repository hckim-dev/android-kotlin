# Credential 설정 전환

소스에 있던 실제 값을 다시 사용하지 마세요. 노출된 서버 Secret과 Token은 먼저 서비스 콘솔에서 폐기/재발급해야 합니다. 이 변경은 Git history를 삭제하지 않습니다.

## Android 로컬 설정

각 앱 루트의 `secrets.properties.example`을 `secrets.properties`로 복사해 필요한 설정만 채우세요. 환경변수가 같은 이름의 로컬 속성보다 우선합니다. 값은 따옴표 없이 작성합니다. 실제 로컬 파일은 Git에서 제외됩니다. 빈 기본값은 빌드 검증용이며 정상적인 외부 서비스 사용을 보장하지 않습니다.

`BuildConfig` 및 Android resource에 들어가는 값은 최종 APK에서 추출할 수 있습니다. 이 파일 분리는 Git 노출 방지 수단입니다. 공개 클라이언트 Key에는 서비스별 앱/API/쿼터 제한이 필요하며 서버 Secret을 여기에 넣으면 안 됩니다.

| 앱 루트 | 설정 이름 |
|---|---|
| `projects/FineDust` | `KAKAO_API_KEY`, `AIR_KOREA_SERVICE_KEY` |
| `projects/Subway` | `SEOUL_API_ACCESS_KEY` |
| `projects/SweetTracker` | `SWEET_TRACKER_API_KEY` |
| `projects/Delivery` | `TMAP_API_KEY`, `GOOGLE_MAPS_API_KEY` |
| `projects/LocationSearchMap` | `TMAP_API_KEY`, `GOOGLE_MAPS_API_KEY` |
| `projects/Airbnb` | `NAVER_MAP_CLIENT_ID` |
| `projects/BookReview` | `INTERPARK_API_KEY` |
| `projects-2023/part2/07-WeatherApp` | `WEATHER_SERVICE_KEY` |
| `projects-2023/part3/05-MediaSearch` | `KAKAO_REST_API_KEY` |
| `projects-2023/part2/09-LocationShare` | `KAKAO_NATIVE_APP_KEY` |
| `projects-2023/part5/ShoppingMallApp` | `KAKAO_NATIVE_APP_KEY` |
| `projects-2023/part2/08-RestaurantMap` | `NAVER_SEARCH_PROXY_BASE_URL`, `NAVER_MAP_CLIENT_ID` |
| `projects/Unsplash` | `UNSPLASH_PROXY_BASE_URL` |
| `projects-2023/part3/04-ImageExtraction` | `UNSPLASH_PROXY_BASE_URL` |

Firebase 사용 앱은 `app/google-services.json.example`을 참고해 해당 앱의 실제 `google-services.json`을 콘솔에서 내려받아 로컬에만 보관하세요. 예제의 빈 API Key 및 가상 프로젝트 값으로 Firebase 인증/DB/지도 기능을 실행할 수 없습니다. 기존 Firebase 공개 식별자는 Secret이 아니므로 그대로 남아 있을 수 있습니다. Delivery/Subway 예제는 원래 파일이 없어서 빌드 구조 설명용 가상 프로젝트로 추가했습니다.

## GitHub 로그인

GitHub OAuth App 설정에서 Device Flow를 활성화해야 합니다. 기존 공개 `GITHUB_CLIENT_ID`는 유지하며 Client Secret은 APK에 포함하지 않습니다. 브라우저에서 사용자 코드를 확인하고 인증하는 흐름입니다. 인증 응답/Authorization HTTP 로그를 끕니다. 실계정 로그인은 이 감사에서 실행하지 않았습니다. 사용자 Access Token은 기존 기기 로컬 저장 방식을 사용하므로 기기 저장소 접근, Android 백업 정책, 로그 정책도 확인하세요.

## Naver 검색 / Unsplash 서버 경유

`NAVER_SEARCH_PROXY_BASE_URL`, `UNSPLASH_PROXY_BASE_URL`은 HTTPS이고 마지막 `/`를 포함한 자체 서버 URL이어야 합니다. 서버는 기존 앱의 상대 경로, 쿼리 및 JSON 응답 형식을 유지하면서 공급자 API를 호출합니다. 앱의 기존 Retrofit Service 인터페이스가 계약입니다. 예를 들어 Naver는 `v1/search/local.json`, Unsplash는 각 앱의 `photos/random` 경로를 지원해야 합니다.

Naver Client Secret과 Unsplash Access Key는 서버 환경변수/Secret Manager에만 둡니다. 이 변경에서는 서버를 배포하거나 운영 Secret을 생성하지 않았습니다. URL 미설정은 요청 시 명시적으로 실패합니다. 임의 URL을 전달받는 공개 프록시로 만들지 말고 공급자 호스트/API를 고정하고 요청 검증, 사용자 인증, 요청 제한, 캐싱, 공급자 쿼터 및 사용량 알림을 적용하세요. 프록시 자체가 비용 발생 경로가 될 수 있습니다.

## 채팅 알림

ChattingApp은 기존 Firebase 채팅 메시지 저장/조회 흐름을 유지하며 앱에서 FCM Server Key로 직접 발송하던 코드를 제거합니다. 따라서 푸시 알림은 서버 구현 전까지 발송되지 않습니다. 서버의 인증된 DB 이벤트 처리기에서 작성자와 채팅방 참여자를 검증하고 신뢰할 수 있는 사용자→기기 Token 저장소를 조회한 뒤 Firebase Admin SDK/HTTP v1로 발송하세요. 클라이언트가 보낸 임의 수신 Token을 그대로 신뢰하지 마세요. 서비스 계정은 서버 IAM/ADC/Secret Manager로 관리하세요.

## Board JWT

`hilt/study/simple-board-main/board`의 Spring 설정은 `JWT_SECRET` 환경변수를 요구하며 소스 기본값이 없습니다. `.env.example`은 설명용입니다. Spring Boot가 `.env` 파일을 자동으로 읽지 않으므로 실제 프로세스 환경 또는 기존 배포 플랫폼의 Secret 주입을 사용하세요. 재발급은 외부 운영 환경에서 직접 수행합니다. 키 변경 시 기존 JWT는 검증에 실패합니다. 원래 만료 시간은 180000000000ms(약 5.7년)이므로 별도 정책에 맞춰 단축을 검토하세요.

## 제한과 후속 조치

- Google Maps: Android package name + 서명 SHA-1 및 해당 Maps SDK API만 허용. 실제 출시/Play App Signing 서명을 사용합니다.
- Firebase: 필요한 Firebase API만 허용하고 IAM, DB/Storage Security Rules, App Check를 검증합니다. 공개 Key에 Gemini/Generative Language 등 다른 유료 API를 허용하지 마세요.
- Kakao Native: 플랫폼 패키지/Key Hash와 OAuth redirect URI를 검증합니다. REST Key의 직접 REST 호출을 Native 앱 Key Hash 제한으로 보호할 수 있다고 가정하지 마세요. 서버 IP 제한은 서버 경유와 함께 사용합니다.
- Naver Maps: Android package 등록 및 사용하는 지도 API, 쿼터/결제를 검증합니다. Naver Search Client Secret은 서버에 둡니다.
- TMAP, 공공데이터, 택배 조회: 직접 앱에 넣는 인증키의 추출 가능성이 남습니다. 노출된 키가 활성 상태면 재발급과 서비스별 쿼터/과금 제한을 확인하고 필요한 경우 서버 경유로 전환하세요. 공급자가 지원한다고 확인되지 않은 패키지/SHA 제한을 가정하지 마세요.
- GitHub push protection과 Secret scan 필수 상태 검사는 저장소 설정에서 활성화해야 합니다. 로컬 스캔은 `docs/credential-security.md`를 참고하세요.

## 공식 문서

- [GitHub OAuth / Device Flow](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps)
- [Firebase API Keys](https://firebase.google.com/docs/projects/api-keys)
- [Google Maps Key 보안](https://developers.google.com/maps/api-security-best-practices)
- [Kakao 앱 설정](https://developers.kakao.com/docs/en/app-setting/app)
- [Naver Maps 앱 설정](https://guide.ncloud-docs.com/docs/en/application-maps-app-vpc)
- [Unsplash API 지침](https://help.unsplash.com/en/articles/2511245-unsplash-api-guidelines)
- [FCM HTTP v1 서버 발송](https://firebase.google.com/docs/cloud-messaging/send/v1-api)
