# DDD 구조 제안서

## 현재 구조 분석

현재 프로젝트는 전통적인 레이어드 아키텍처를 따르고 있습니다:
```
com.lian.shop/
├── controller/     # 프레젠테이션 계층
├── service/        # 비즈니스 로직 계층
├── repository/     # 데이터 접근 계층
├── domain/         # 도메인 엔티티
├── dto/            # 데이터 전송 객체
├── client/         # 외부 API 클라이언트
└── config/         # 설정
```

**현재 구조의 장점:**
- ✅ 도메인 엔티티에 일부 비즈니스 로직 포함 (Order.changeStatus, Order.markAsPaid 등)
- ✅ 계층이 명확하게 분리되어 있음
- ✅ 이해하기 쉬운 구조

**개선이 필요한 부분:**
- ❌ Repository 인터페이스가 domain 계층이 아닌 별도 계층에 있음
- ❌ Service가 도메인 로직과 애플리케이션 로직이 혼재
- ❌ 외부 의존성(토스페이먼츠 클라이언트)이 도메인에 직접 노출될 수 있음

---

## 제안 1: 표준 DDD 구조 (권장)

### 구조

```
com.lian.shop/
├── domain/                    # 도메인 계층 (핵심 비즈니스 로직)
│   ├── product/
│   │   ├── Product.kt         # 엔티티
│   │   ├── ProductRepository.kt  # 리포지토리 인터페이스
│   │   └── ProductDomainService.kt  # 도메인 서비스 (필요시)
│   ├── order/
│   │   ├── Order.kt
│   │   ├── OrderItem.kt
│   │   ├── OrderStatus.kt     # 값 객체 또는 Enum
│   │   ├── OrderRepository.kt
│   │   └── OrderDomainService.kt
│   ├── payment/
│   │   ├── Payment.kt
│   │   ├── PaymentStatus.kt
│   │   ├── PaymentRepository.kt
│   │   └── PaymentDomainService.kt
│   └── user/
│       ├── User.kt
│       ├── UserRepository.kt
│       └── UserDomainService.kt
│
├── application/               # 애플리케이션 계층 (유스케이스)
│   ├── product/
│   │   ├── ProductService.kt      # 애플리케이션 서비스
│   │   └── ProductDto.kt          # DTO
│   ├── order/
│   │   ├── OrderService.kt
│   │   └── OrderDto.kt
│   ├── payment/
│   │   ├── PaymentService.kt
│   │   └── PaymentDto.kt
│   └── auth/
│       ├── AuthService.kt
│       └── AuthDto.kt
│
├── infrastructure/            # 인프라스트럭처 계층 (기술적 구현)
│   ├── persistence/           # 영속성 구현
│   │   ├── product/
│   │   │   └── JpaProductRepository.kt  # JPA 구현
│   │   ├── order/
│   │   │   └── JpaOrderRepository.kt
│   │   └── payment/
│   │       └── JpaPaymentRepository.kt
│   ├── external/              # 외부 서비스 연동
│   │   └── (토스페이먼츠 관련 파일 제거됨 - 네이버페이만 사용)
│   └── config/                # 설정
│       ├── SecurityConfig.kt
│       ├── WebConfig.kt
│       └── ...
│
└── presentation/              # 프레젠테이션 계층 (API)
    ├── product/
    │   └── ProductController.kt
    ├── order/
    │   └── OrderController.kt
    ├── payment/
    │   └── PaymentController.kt
    └── auth/
        └── AuthController.kt
```

### 계층별 역할

#### 1. Domain (도메인 계층)
- **책임**: 핵심 비즈니스 로직, 도메인 규칙
- **포함**: 엔티티, 값 객체, 도메인 서비스, 리포지토리 인터페이스
- **의존성**: 다른 계층에 의존하지 않음 (순수한 비즈니스 로직)
- **예시**:
  ```kotlin
  // domain/order/Order.kt
  class Order {
      fun changeStatus(newStatus: OrderStatus, reason: String) {
          // 도메인 규칙 검증
          if (status == OrderStatus.DELIVERED) {
              throw IllegalStateException("배송 완료된 주문은 상태 변경 불가")
          }
          // 상태 변경 로직
      }
  }
  
  // domain/order/OrderRepository.kt (인터페이스만)
  interface OrderRepository {
      fun save(order: Order): Order
      fun findById(id: Long): Order?
      fun findByUserId(userId: Long): List<Order>
  }
  ```

#### 2. Application (애플리케이션 계층)
- **책임**: 유스케이스 조율, 트랜잭션 관리, 도메인 객체 조합
- **포함**: 애플리케이션 서비스, DTO, 유스케이스
- **의존성**: Domain 계층에만 의존
- **예시**:
  ```kotlin
  // application/order/OrderService.kt
  @Service
  class OrderService(
      private val orderRepository: OrderRepository,  // 인터페이스
      private val productRepository: ProductRepository,
      private val userRepository: UserRepository
  ) {
      @Transactional
      fun createOrder(request: CreateOrderRequest): OrderDto {
          // 여러 도메인 객체를 조합하여 유스케이스 실행
          val user = userRepository.findById(request.userId)
          val order = Order(...)
          // ...
          return orderRepository.save(order).toDto()
      }
  }
  ```

#### 3. Infrastructure (인프라스트럭처 계층)
- **책임**: 기술적 구현 세부사항
- **포함**: JPA 리포지토리 구현, 외부 API 클라이언트, 설정
- **의존성**: Domain, Application 계층에 의존
- **예시**:
  ```kotlin
  // infrastructure/persistence/order/JpaOrderRepository.kt
  @Repository
  interface JpaOrderRepository : JpaRepository<Order, Long>, OrderRepository {
      // JPA 구현이 자동으로 OrderRepository 인터페이스를 구현
  }
  ```

#### 4. Presentation (프레젠테이션 계층)
- **책임**: HTTP 요청/응답 처리
- **포함**: 컨트롤러
- **의존성**: Application 계층에만 의존
- **예시**:
  ```kotlin
  // presentation/order/OrderController.kt
  @RestController
  class OrderController(
      private val orderService: OrderService  // 애플리케이션 서비스
  ) {
      @PostMapping("/api/orders")
      fun createOrder(@RequestBody request: CreateOrderRequest): OrderDto {
          return orderService.createOrder(request)
      }
  }
  ```

### 의존성 규칙

```
Presentation → Application → Domain
Infrastructure → Domain, Application
```

- **Domain**: 다른 계층에 의존하지 않음
- **Application**: Domain에만 의존
- **Infrastructure**: Domain, Application에 의존
- **Presentation**: Application에만 의존

---

## 제안 2: 간소화된 DDD 구조 (점진적 마이그레이션)

현재 구조를 크게 변경하지 않고 점진적으로 개선하는 방법:

```
com.lian.shop/
├── domain/                    # 도메인 계층
│   ├── product/
│   │   ├── Product.kt
│   │   └── ProductRepository.kt  # 인터페이스만
│   ├── order/
│   │   ├── Order.kt
│   │   └── OrderRepository.kt
│   └── ...
│
├── application/               # 애플리케이션 계층
│   ├── product/
│   │   ├── ProductService.kt
│   │   └── ProductDto.kt
│   └── ...
│
├── infrastructure/            # 인프라스트럭처 계층
│   ├── repository/           # JPA 리포지토리 구현
│   │   ├── JpaProductRepository.kt
│   │   └── ...
│   ├── external/             # 외부 서비스 연동
│   │   └── naverpay/        # 네이버페이 클라이언트
│   └── config/               # 설정
│
└── presentation/             # 프레젠테이션 계층
    └── controller/
```

**장점:**
- 기존 코드와 유사하여 마이그레이션 부담이 적음
- 도메인별 패키지 구조로 확장성 확보

---

## 제안 3: 하이브리드 구조 (현실적 접근)

현재 구조를 최대한 유지하면서 DDD 원칙만 적용:

```
com.lian.shop/
├── domain/                    # 도메인 계층
│   ├── Product.kt
│   ├── Order.kt
│   ├── Payment.kt
│   └── repository/           # 리포지토리 인터페이스
│       ├── ProductRepository.kt
│       ├── OrderRepository.kt
│       └── ...
│
├── application/               # 애플리케이션 계층
│   ├── service/              # 애플리케이션 서비스
│   │   ├── ProductService.kt
│   │   ├── OrderService.kt
│   │   └── ...
│   └── dto/                  # DTO
│       ├── ProductDto.kt
│       └── ...
│
├── infrastructure/            # 인프라스트럭처 계층
│   ├── repository/           # JPA 구현
│   │   └── JpaRepositories.kt
│   ├── external/             # 외부 서비스 연동
│   │   └── naverpay/        # 네이버페이 클라이언트
│   └── config/
│
└── presentation/             # 프레젠테이션 계층
    └── controller/
```

**장점:**
- 기존 구조와 가장 유사
- 최소한의 변경으로 DDD 원칙 적용
- 팀원들이 쉽게 적응 가능

---

## 마이그레이션 전략

### 1단계: Repository 인터페이스를 Domain으로 이동
- `repository/Repositories.kt`의 인터페이스들을 `domain/repository/`로 이동
- JPA 구현은 `infrastructure/repository/`에 유지

### 2단계: Service를 Application으로 이동
- `service/` → `application/service/`로 이동
- 도메인 로직은 Domain 계층으로 이동 검토

### 3단계: Controller를 Presentation으로 이동
- `controller/` → `presentation/controller/`로 이동

### 4단계: Client를 Infrastructure로 이동
- `client/` → `infrastructure/external/`로 이동

### 5단계: DTO를 Application으로 이동
- `dto/` → `application/dto/`로 이동

---

## 권장사항

### ✅ 추천: 제안 3 (하이브리드 구조)

**이유:**
1. **점진적 마이그레이션**: 기존 코드를 크게 변경하지 않고 적용 가능
2. **실용적**: 작은 팀이나 MVP 단계에서도 적용 가능
3. **명확한 계층 분리**: DDD 원칙을 지키면서도 복잡도 최소화
4. **확장성**: 나중에 제안 1로 발전 가능

### 적용 우선순위

1. **High**: Repository 인터페이스를 Domain으로 이동
   - 도메인 계층의 독립성 확보
   - 의존성 역전 원칙 적용

2. **Medium**: Service를 Application으로 이동
   - 애플리케이션 계층 명확화
   - 도메인 로직과 애플리케이션 로직 분리

3. **Low**: Controller를 Presentation으로 이동
   - 구조적 완성도 향상
   - 기능에는 영향 없음

---

## 예상 효과

### 장점
- ✅ **도메인 독립성**: 비즈니스 로직이 기술적 세부사항에 의존하지 않음
- ✅ **테스트 용이성**: 도메인 로직을 독립적으로 테스트 가능
- ✅ **유지보수성**: 계층별 책임이 명확하여 변경 영향 범위 파악 용이
- ✅ **확장성**: 새로운 기능 추가 시 적절한 계층에 배치 가능

### 주의사항
- ⚠️ **초기 복잡도 증가**: 작은 프로젝트에서는 오버엔지니어링일 수 있음
- ⚠️ **학습 곡선**: 팀원들의 DDD 이해 필요
- ⚠️ **마이그레이션 비용**: 기존 코드 리팩토링 필요

---

## 다음 단계

1. 팀과 논의하여 구조 선택
2. 작은 도메인(예: Product)부터 시작하여 마이그레이션
3. 점진적으로 다른 도메인에 적용
4. 리팩토링 과정에서 테스트 코드 작성/보완

