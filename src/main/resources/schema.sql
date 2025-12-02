CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 주문 정보 테이블
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL, -- PENDING, PAYMENT_FAILED, PAID, PREPARING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address TEXT NOT NULL,
    order_number VARCHAR(100) UNIQUE, -- 주문번호 (토스페이먼츠 orderId와 연동)
    customer_name VARCHAR(100), -- 주문자명
    customer_email VARCHAR(255), -- 주문자 이메일
    customer_phone VARCHAR(50), -- 주문자 전화번호
    payment_deadline TIMESTAMP, -- 결제 기한 (가상계좌 등)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP, -- 취소 시간
    cancel_reason TEXT, -- 취소 사유
    metadata TEXT -- 추가 메타데이터 (JSON 형식)
);

-- 주문 상태 변경 이력 테이블
CREATE TABLE IF NOT EXISTS order_histories (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    previous_status VARCHAR(50), -- 이전 상태
    new_status VARCHAR(50) NOT NULL, -- 새로운 상태
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 변경 시간
    changed_by VARCHAR(100), -- 변경 주체 (시스템, 사용자 ID 등)
    reason TEXT, -- 상태 변경 사유
    payment_id INT, -- 결제와 연관된 경우 결제 ID
    metadata TEXT -- 추가 메타데이터 (JSON 형식)
);

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    product_id INT REFERENCES products(id),
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL
);

-- 결제 정보 테이블
CREATE TABLE IF NOT EXISTS payments (
    id SERIAL PRIMARY KEY,
    order_id INT UNIQUE REFERENCES orders(id),
    payment_key VARCHAR(200) UNIQUE, -- 토스페이먼츠 결제 키
    order_id_toss VARCHAR(100), -- 토스페이먼츠 주문번호
    order_name VARCHAR(255), -- 주문명
    amount DECIMAL(10, 2) NOT NULL, -- 결제 금액
    balance_amount DECIMAL(10, 2), -- 잔액 (취소 후 남은 금액)
    supplied_amount DECIMAL(10, 2), -- 공급가액
    vat DECIMAL(10, 2), -- 부가세
    tax_free_amount DECIMAL(10, 2), -- 면세 금액
    tax_exemption_amount DECIMAL(10, 2), -- 과세 제외 금액
    status VARCHAR(50) NOT NULL, -- READY, IN_PROGRESS, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED
    method VARCHAR(50), -- 결제 수단 (카드, 계좌이체, 가상계좌 등)
    currency VARCHAR(10) DEFAULT 'KRW', -- 통화
    m_id VARCHAR(100), -- 상점 ID
    version VARCHAR(50), -- API 버전
    requested_at TIMESTAMP, -- 결제 요청 시간
    approved_at TIMESTAMP, -- 결제 승인 시간
    use_escrow BOOLEAN DEFAULT FALSE, -- 에스크로 사용 여부
    culture_expense BOOLEAN DEFAULT FALSE, -- 문화비 지출 여부
    metadata TEXT, -- 추가 메타데이터 (JSON 형식)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 결제 상태 변경 이력 테이블
CREATE TABLE IF NOT EXISTS payment_histories (
    id SERIAL PRIMARY KEY,
    payment_id INT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    previous_status VARCHAR(50), -- 이전 상태
    new_status VARCHAR(50) NOT NULL, -- 새로운 상태
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 변경 시간
    changed_by VARCHAR(100), -- 변경 주체 (시스템, 사용자 ID 등)
    reason TEXT, -- 상태 변경 사유
    metadata TEXT -- 추가 메타데이터 (JSON 형식)
);

-- 결제 취소/환불 이력 테이블
CREATE TABLE IF NOT EXISTS payment_cancels (
    id SERIAL PRIMARY KEY,
    payment_id INT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    cancel_amount DECIMAL(10, 2) NOT NULL, -- 취소 금액
    cancel_reason TEXT NOT NULL, -- 취소 사유
    tax_free_amount DECIMAL(10, 2), -- 면세 금액
    tax_exemption_amount DECIMAL(10, 2), -- 과세 제외 금액
    refundable_amount DECIMAL(10, 2), -- 환불 가능 금액
    transaction_key VARCHAR(100), -- 토스페이먼츠 거래 키
    receipt_key VARCHAR(100), -- 토스페이먼츠 영수증 키
    cancel_status VARCHAR(50) NOT NULL, -- IN_PROGRESS, DONE, ABORTED
    cancel_request_id VARCHAR(100), -- 멱등성을 위한 취소 요청 ID
    canceled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 취소 시간
    refund_receive_account TEXT, -- 환불 계좌 정보 (JSON 형식)
    metadata TEXT -- 추가 메타데이터 (JSON 형식)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_histories_order_id ON order_histories(order_id);
CREATE INDEX IF NOT EXISTS idx_order_histories_changed_at ON order_histories(changed_at);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_key ON payments(payment_key);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_histories_payment_id ON payment_histories(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_histories_changed_at ON payment_histories(changed_at);
CREATE INDEX IF NOT EXISTS idx_payment_cancels_payment_id ON payment_cancels(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_cancels_transaction_key ON payment_cancels(transaction_key);
CREATE INDEX IF NOT EXISTS idx_payment_cancels_canceled_at ON payment_cancels(canceled_at);

CREATE TABLE IF NOT EXISTS shipments (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    tracking_number VARCHAR(255),
    carrier VARCHAR(100),
    status VARCHAR(50) NOT NULL, -- PREPARING, SHIPPED, DELIVERED
    shipped_at TIMESTAMP
);

-- ============================================
-- 테이블 코멘트
-- ============================================

COMMENT ON TABLE users IS '사용자 정보 테이블';
COMMENT ON COLUMN users.id IS '사용자 ID (PK)';
COMMENT ON COLUMN users.email IS '이메일 주소 (UNIQUE)';
COMMENT ON COLUMN users.password IS '비밀번호 (암호화)';
COMMENT ON COLUMN users.name IS '사용자 이름';
COMMENT ON COLUMN users.role IS '사용자 역할 (CUSTOMER, ADMIN)';
COMMENT ON COLUMN users.created_at IS '계정 생성 시간';

COMMENT ON TABLE products IS '상품 정보 테이블';
COMMENT ON COLUMN products.id IS '상품 ID (PK)';
COMMENT ON COLUMN products.name IS '상품명';
COMMENT ON COLUMN products.description IS '상품 설명';
COMMENT ON COLUMN products.price IS '상품 가격';
COMMENT ON COLUMN products.stock_quantity IS '재고 수량';
COMMENT ON COLUMN products.image_url IS '상품 이미지 URL';
COMMENT ON COLUMN products.created_at IS '상품 등록 시간';

COMMENT ON TABLE orders IS '주문 정보 테이블 - 결제와 연동되어 상태가 자동 관리됨';
COMMENT ON COLUMN orders.id IS '주문 ID (PK)';
COMMENT ON COLUMN orders.user_id IS '주문자 ID (FK)';
COMMENT ON COLUMN orders.status IS '주문 상태 (PENDING, PAYMENT_FAILED, PAID, PREPARING, SHIPPED, DELIVERED, CANCELLED, REFUNDED)';
COMMENT ON COLUMN orders.total_amount IS '주문 총 금액';
COMMENT ON COLUMN orders.shipping_address IS '배송 주소';
COMMENT ON COLUMN orders.order_number IS '주문번호 (토스페이먼츠 orderId와 연동, UNIQUE)';
COMMENT ON COLUMN orders.customer_name IS '주문자명';
COMMENT ON COLUMN orders.customer_email IS '주문자 이메일';
COMMENT ON COLUMN orders.customer_phone IS '주문자 전화번호';
COMMENT ON COLUMN orders.payment_deadline IS '결제 기한 (가상계좌 등)';
COMMENT ON COLUMN orders.created_at IS '주문 생성 시간';
COMMENT ON COLUMN orders.updated_at IS '주문 수정 시간';
COMMENT ON COLUMN orders.cancelled_at IS '주문 취소 시간';
COMMENT ON COLUMN orders.cancel_reason IS '주문 취소 사유';
COMMENT ON COLUMN orders.metadata IS '추가 메타데이터 (JSON 형식)';

COMMENT ON TABLE order_histories IS '주문 상태 변경 이력 테이블 - 모든 주문 상태 변경을 추적';
COMMENT ON COLUMN order_histories.id IS '이력 ID (PK)';
COMMENT ON COLUMN order_histories.order_id IS '주문 ID (FK)';
COMMENT ON COLUMN order_histories.previous_status IS '이전 주문 상태';
COMMENT ON COLUMN order_histories.new_status IS '새로운 주문 상태';
COMMENT ON COLUMN order_histories.changed_at IS '상태 변경 시간';
COMMENT ON COLUMN order_histories.changed_by IS '변경 주체 (SYSTEM, 사용자 ID 등)';
COMMENT ON COLUMN order_histories.reason IS '상태 변경 사유';
COMMENT ON COLUMN order_histories.payment_id IS '결제와 연관된 경우 결제 ID';
COMMENT ON COLUMN order_histories.metadata IS '추가 메타데이터 (JSON 형식)';

COMMENT ON TABLE order_items IS '주문 상품 목록 테이블';
COMMENT ON COLUMN order_items.id IS '주문 상품 ID (PK)';
COMMENT ON COLUMN order_items.order_id IS '주문 ID (FK)';
COMMENT ON COLUMN order_items.product_id IS '상품 ID (FK)';
COMMENT ON COLUMN order_items.quantity IS '주문 수량';
COMMENT ON COLUMN order_items.price_at_purchase IS '주문 당시 상품 가격 (가격 변동 대비)';

COMMENT ON TABLE payments IS '결제 정보 테이블 - 토스페이먼츠 결제 승인 정보 저장';
COMMENT ON COLUMN payments.id IS '결제 ID (PK)';
COMMENT ON COLUMN payments.order_id IS '주문 ID (FK, UNIQUE)';
COMMENT ON COLUMN payments.payment_key IS '토스페이먼츠 결제 키 (UNIQUE)';
COMMENT ON COLUMN payments.order_id_toss IS '토스페이먼츠 주문번호';
COMMENT ON COLUMN payments.order_name IS '주문명';
COMMENT ON COLUMN payments.amount IS '결제 금액';
COMMENT ON COLUMN payments.balance_amount IS '잔액 (취소 후 남은 금액)';
COMMENT ON COLUMN payments.supplied_amount IS '공급가액';
COMMENT ON COLUMN payments.vat IS '부가세';
COMMENT ON COLUMN payments.tax_free_amount IS '면세 금액';
COMMENT ON COLUMN payments.tax_exemption_amount IS '과세 제외 금액';
COMMENT ON COLUMN payments.status IS '결제 상태 (READY, IN_PROGRESS, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)';
COMMENT ON COLUMN payments.method IS '결제 수단 (카드, 계좌이체, 가상계좌 등)';
COMMENT ON COLUMN payments.currency IS '통화 (KRW, USD 등)';
COMMENT ON COLUMN payments.m_id IS '상점 ID (Merchant ID)';
COMMENT ON COLUMN payments.version IS '토스페이먼츠 API 버전';
COMMENT ON COLUMN payments.requested_at IS '결제 요청 시간';
COMMENT ON COLUMN payments.approved_at IS '결제 승인 시간';
COMMENT ON COLUMN payments.use_escrow IS '에스크로 사용 여부';
COMMENT ON COLUMN payments.culture_expense IS '문화비 지출 여부';
COMMENT ON COLUMN payments.metadata IS '추가 메타데이터 (JSON 형식)';
COMMENT ON COLUMN payments.created_at IS '결제 생성 시간';
COMMENT ON COLUMN payments.updated_at IS '결제 수정 시간';

COMMENT ON TABLE payment_histories IS '결제 상태 변경 이력 테이블 - 모든 결제 상태 변경을 추적';
COMMENT ON COLUMN payment_histories.id IS '이력 ID (PK)';
COMMENT ON COLUMN payment_histories.payment_id IS '결제 ID (FK)';
COMMENT ON COLUMN payment_histories.previous_status IS '이전 결제 상태';
COMMENT ON COLUMN payment_histories.new_status IS '새로운 결제 상태';
COMMENT ON COLUMN payment_histories.changed_at IS '상태 변경 시간';
COMMENT ON COLUMN payment_histories.changed_by IS '변경 주체 (SYSTEM, 사용자 ID 등)';
COMMENT ON COLUMN payment_histories.reason IS '상태 변경 사유';
COMMENT ON COLUMN payment_histories.metadata IS '추가 메타데이터 (JSON 형식)';

COMMENT ON TABLE payment_cancels IS '결제 취소/환불 이력 테이블 - 부분 취소 지원, 각 취소 건마다 별도 레코드';
COMMENT ON COLUMN payment_cancels.id IS '취소 ID (PK)';
COMMENT ON COLUMN payment_cancels.payment_id IS '결제 ID (FK)';
COMMENT ON COLUMN payment_cancels.cancel_amount IS '취소 금액';
COMMENT ON COLUMN payment_cancels.cancel_reason IS '취소 사유';
COMMENT ON COLUMN payment_cancels.tax_free_amount IS '면세 금액';
COMMENT ON COLUMN payment_cancels.tax_exemption_amount IS '과세 제외 금액';
COMMENT ON COLUMN payment_cancels.refundable_amount IS '환불 가능 금액';
COMMENT ON COLUMN payment_cancels.transaction_key IS '토스페이먼츠 거래 키';
COMMENT ON COLUMN payment_cancels.receipt_key IS '토스페이먼츠 영수증 키';
COMMENT ON COLUMN payment_cancels.cancel_status IS '취소 상태 (IN_PROGRESS, DONE, ABORTED)';
COMMENT ON COLUMN payment_cancels.cancel_request_id IS '멱등성을 위한 취소 요청 ID';
COMMENT ON COLUMN payment_cancels.canceled_at IS '취소 시간';
COMMENT ON COLUMN payment_cancels.refund_receive_account IS '환불 계좌 정보 (JSON 형식)';
COMMENT ON COLUMN payment_cancels.metadata IS '추가 메타데이터 (JSON 형식)';

COMMENT ON TABLE shipments IS '배송 정보 테이블';
COMMENT ON COLUMN shipments.id IS '배송 ID (PK)';
COMMENT ON COLUMN shipments.order_id IS '주문 ID (FK)';
COMMENT ON COLUMN shipments.tracking_number IS '운송장 번호';
COMMENT ON COLUMN shipments.carrier IS '배송사';
COMMENT ON COLUMN shipments.status IS '배송 상태 (PREPARING, SHIPPED, DELIVERED)';
COMMENT ON COLUMN shipments.shipped_at IS '배송 시작 시간';
