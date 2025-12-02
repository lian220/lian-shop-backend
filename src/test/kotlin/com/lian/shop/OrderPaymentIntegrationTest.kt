package com.lian.shop

import com.lian.shop.domain.*
import com.lian.shop.dto.*
import com.lian.shop.repository.*
import com.lian.shop.service.AuthService
import com.lian.shop.service.OrderService
import com.lian.shop.service.PaymentService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.comparables.shouldBeGreaterThan
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import jakarta.persistence.EntityManager
import java.math.BigDecimal

@SpringBootTest
@Transactional
class OrderPaymentIntegrationTest(
    @Autowired private val authService: AuthService,
    @Autowired private val orderService: OrderService,
    @Autowired private val paymentService: PaymentService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val orderRepository: OrderRepository,
    @Autowired private val paymentRepository: PaymentRepository,
    @Autowired private val entityManager: EntityManager
) : DescribeSpec({

    describe("주문 및 결제 통합 테스트") {
        val testEmail = "lian.dy221@gmail.com"
        val testPassword = "sfn0008"
        lateinit var testUser: User
        lateinit var testProduct: Product
        var userId: Long? = null

        beforeSpec {
            // 테스트 사용자 생성 또는 조회
            val foundUser = userRepository.findByEmail(testEmail)
            testUser = if (foundUser != null) {
                foundUser
            } else {
                val newUser = User(
                    email = testEmail,
                    password = testPassword,
                    name = "테스트 사용자",
                    role = Role.CUSTOMER
                )
                val savedUser = userRepository.save(newUser)
                entityManager.flush()
                entityManager.refresh(savedUser)
                savedUser
            }
            userId = testUser.id ?: throw IllegalStateException("사용자 ID가 생성되지 않았습니다")

            // 테스트 상품 생성 또는 조회
            val foundProduct = productRepository.findById(1L).orElse(null)
            testProduct = if (foundProduct != null) {
                foundProduct
            } else {
                val newProduct = Product(
                    name = "MacBook Pro",
                    description = "Apple M3 Pro chip, 16GB RAM",
                    price = BigDecimal("2499.99"),
                    stockQuantity = 10,
                    imageUrl = "https://example.com/macbook.jpg"
                )
                val savedProduct = productRepository.save(newProduct)
                productRepository.flush()
                savedProduct
            }
        }

        context("상품 조회") {
            it("생성된 상품을 조회할 수 있어야 한다") {
                testProduct.id shouldNotBe null
                val product = productRepository.findById(testProduct.id!!)
                product.isPresent shouldBe true
                product.get().name shouldNotBe null
            }
        }

        context("주문 생성") {
            var orderDto: OrderDto? = null

            it("상품 1개를 주문할 수 있어야 한다") {
                val createOrderRequest = CreateOrderRequest(
                    userId = testUser.id ?: throw IllegalStateException("사용자 ID가 설정되지 않았습니다"),
                    items = listOf(
                        CreateOrderItemRequest(
                            productId = testProduct.id!!,
                            quantity = 1
                        )
                    ),
                    shippingAddress = "서울시 강남구 테헤란로 123",
                    customerName = "테스트 사용자",
                    customerEmail = testEmail,
                    customerPhone = "010-1234-5678"
                )

                orderDto = orderService.createOrder(createOrderRequest)

                orderDto shouldNotBe null
                orderDto!!.id shouldNotBe null
                orderDto!!.status shouldBe "PENDING"
                orderDto!!.totalAmount shouldBe testProduct.price
                orderDto!!.orderNumber shouldNotBe null
            }

            it("주문번호가 생성되어야 한다") {
                orderDto!!.orderNumber shouldNotBe null
                orderDto!!.orderNumber!!.startsWith("order_") shouldBe true
            }

            it("주문 상태가 PENDING이어야 한다") {
                val order = orderRepository.findById(orderDto!!.id!!)
                order.isPresent shouldBe true
                order.get().status shouldBe OrderStatus.PENDING
            }
        }

        context("결제 승인 (테스트용)") {
            var orderDto: OrderDto? = null
            var paymentResponse: ConfirmPaymentResponse? = null

            beforeEach {
                // 주문 생성
                val createOrderRequest = CreateOrderRequest(
                    userId = testUser.id ?: throw IllegalStateException("사용자 ID가 설정되지 않았습니다"),
                    items = listOf(
                        CreateOrderItemRequest(
                            productId = testProduct.id!!,
                            quantity = 1
                        )
                    ),
                    shippingAddress = "서울시 강남구 테헤란로 123",
                    customerName = "테스트 사용자",
                    customerEmail = testEmail,
                    customerPhone = "010-1234-5678"
                )

                orderDto = orderService.createOrder(createOrderRequest)
            }

            it("테스트용 결제 승인을 할 수 있어야 한다") {
                val confirmPaymentRequest = ConfirmPaymentRequest(
                    paymentKey = "test_payment_key_${System.currentTimeMillis()}",
                    orderId = orderDto!!.orderNumber!!,
                    amount = orderDto!!.totalAmount.toLong()
                )

                paymentResponse = paymentService.confirmPaymentForTest(confirmPaymentRequest)

                paymentResponse shouldNotBe null
                paymentResponse!!.status shouldBe "DONE"
                paymentResponse!!.paymentKey shouldBe confirmPaymentRequest.paymentKey
                paymentResponse!!.orderId shouldBe confirmPaymentRequest.orderId
            }

            it("결제 승인 후 주문 상태가 PAID로 변경되어야 한다") {
                val confirmPaymentRequest = ConfirmPaymentRequest(
                    paymentKey = "test_payment_key_${System.currentTimeMillis()}",
                    orderId = orderDto!!.orderNumber!!,
                    amount = orderDto!!.totalAmount.toLong()
                )

                paymentService.confirmPaymentForTest(confirmPaymentRequest)

                val order = orderRepository.findById(orderDto!!.id!!)
                order.isPresent shouldBe true
                order.get().status shouldBe OrderStatus.PAID
            }

            it("결제 정보가 저장되어야 한다") {
                val confirmPaymentRequest = ConfirmPaymentRequest(
                    paymentKey = "test_payment_key_${System.currentTimeMillis()}",
                    orderId = orderDto!!.orderNumber!!,
                    amount = orderDto!!.totalAmount.toLong()
                )

                paymentService.confirmPaymentForTest(confirmPaymentRequest)

                val payment = paymentRepository.findByOrderId(orderDto!!.id!!)
                payment shouldNotBe null
                payment!!.status shouldBe PaymentStatus.DONE
                payment.paymentKey shouldBe confirmPaymentRequest.paymentKey
            }

            it("결제 이력이 생성되어야 한다") {
                val confirmPaymentRequest = ConfirmPaymentRequest(
                    paymentKey = "test_payment_key_${System.currentTimeMillis()}",
                    orderId = orderDto!!.orderNumber!!,
                    amount = orderDto!!.totalAmount.toLong()
                )

                paymentService.confirmPaymentForTest(confirmPaymentRequest)

                val payment = paymentRepository.findByOrderId(orderDto!!.id!!)
                payment shouldNotBe null
                payment!!.histories.size shouldBe 1
                payment.histories[0].newStatus shouldBe PaymentStatus.DONE
            }

            it("주문 이력이 생성되어야 한다") {
                val confirmPaymentRequest = ConfirmPaymentRequest(
                    paymentKey = "test_payment_key_${System.currentTimeMillis()}",
                    orderId = orderDto!!.orderNumber!!,
                    amount = orderDto!!.totalAmount.toLong()
                )

                paymentService.confirmPaymentForTest(confirmPaymentRequest)

                val order = orderRepository.findById(orderDto!!.id!!)
                order.isPresent shouldBe true
                order.get().histories.size shouldBeGreaterThan 0
            }
        }

        context("금액 검증") {
            it("주문 금액과 결제 금액이 다르면 실패해야 한다") {
                val createOrderRequest = CreateOrderRequest(
                    userId = testUser.id ?: throw IllegalStateException("사용자 ID가 설정되지 않았습니다"),
                    items = listOf(
                        CreateOrderItemRequest(
                            productId = testProduct.id!!,
                            quantity = 1
                        )
                    ),
                    shippingAddress = "서울시 강남구 테헤란로 123"
                )

                val orderDto = orderService.createOrder(createOrderRequest)

                val confirmPaymentRequest = ConfirmPaymentRequest(
                    paymentKey = "test_payment_key",
                    orderId = orderDto.orderNumber!!,
                    amount = orderDto.totalAmount.toLong() + 1000 // 잘못된 금액
                )

                try {
                    paymentService.confirmPaymentForTest(confirmPaymentRequest)
                    throw AssertionError("예외가 발생해야 합니다")
                } catch (e: RuntimeException) {
                    e.message shouldNotBe null
                    e.message!!.contains("금액") shouldBe true
                }
            }
        }
    }
})

