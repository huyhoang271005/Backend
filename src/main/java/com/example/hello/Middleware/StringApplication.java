package com.example.hello.Middleware;


public class StringApplication {
    public static class SUCCESS {
        public static final String LOGIN_SUCCESS = "Đăng nhập thành công";
        public static final String REGISTER_SUCCESS = "Đăng ký thành công";
        public static final String CHECK_EMAIL = "Vui lòng kiểm tra email của bạn";
    }
    public static class ERROR {
        public static final String NUMBER = "Số lượng phải lớn hơn 0";
        public static final String MONEY = "Giá tiền phải lớn hơn 0";
        public static final String FORBIDDEN = "Không đủ quyền truy cập";
        public static final String NEW_DEVICE = "Phát hiện đăng nhập trên thiết bị mới";
        public static final String INPUT_INVALID = "Dữ liệu đầu vào không hợp lệ";
        public static final String INTERNAL_SERVER_ERROR = "Lỗi máy chủ";
        public static final String PASSWORD_OR_EMAIL_INCORRECT = "Email hoặc mật khẩu không đúng";
        public static final String VERIFIED_EMAIL_MUST_EXIST = "Cần tồn tại ít nhất 1 email đã được xác thực";
        public static final String ACCOUNT_LOCKED = "Tài khoản đã bị khoá";
        public static final String ACCOUNT_PENDING = "Tài khoản đang chờ được xác thực";
        public static final String CANT_LOGIN = "Tài khoản của bạn hiện không thể đăng nhập";
        public static final String USER_NOT_LOGIN = "Người dùng chưa đăng nhập";
        public static final String UPLOAD_ERROR = "Ảnh tải lên không hợp lệ";
        public static final String UPLOAD_IO_ERROR = "Lỗi xử lý ảnh";
        public static final String DELETE_IO_ERROR = "Lỗi xoá ảnh";
    }

    public static class FIELD {
        public static final String CANT_CANCEL = "Không thể huỷ";
        public static final String ORDER = "Đơn hàng";
        public static final String CART = "Giỏ hàng";
        public static final String MAXIMUM = " đã đạt đến giới hạn";
        public static final String CONTACT = "Liên hệ";
        public static final String PRODUCT = "Sản phẩm";
        public static final String ATTRIBUTE = "Thuộc tính";
        public static final String BRAND = "Thương hiệu";
        public static final String CATEGORY = "Danh mục";
        public static final String SESSION_LOGIN = "Phiên đăng nhập";
        public static final String USER = "Người dùng";
        public static final String CANT_REMOVE = "Không thể xoá";
        public static final String ROLE = "Chức vụ";
        public static final String ROLE_PERMISSION = "Quyền hạn cho chức vụ này";
        public static final String CHANGE_PASSWORD = "Thay đổi mật khẩu ";
        public static final String WAIT_AFTER = "Vui lòng thử lại sau ";
        public static final String SOME = "ít ";
        public static final String SECONDS= " giây";
        public static final String MINUTES= " phút";
        public static final String HOURS = " giờ";
        public static final String VERIFY = "Xác thực ";
        public static final String DEVICE = "Thiết bị";
        public static final String UNVERIFIED = " chưa được xác thực";
        public static final String EXISTED = " đã tồn tại";
        public static final String NOT_EXIST = " không tồn tại";
        public static final String VERIFIED_SUCCESS = "Đã xác thực thành công";
        public static final String SUCCESS = "Thành công";
        public static final String REQUEST = "Yêu cầu";
        public static final String EXPIRED = " đã hết hiệu lực";
        public static final String INVALID = " không hợp lệ";
        public static final String NOT_EMPTY = " không được bỏ trông";
        public static final String EMAIL = "Email";
        public static final String VERIFIED = " đã được xác thực";
        public static final String PASSWORD = "Mật khẩu";
        public static final String USERNAME = "Biệt danh";
        public static final String FULL_NAME = "Tên đầy đủ";
        public static final String LOGIN_NEW_DEVICE = "Đăng nhập trên thiết bị mới";
        public static final String ADD_NEW_EMAIL = "Thêm email mới";
        public static final String DATE = "Ngày";
        public static final String TIME = "Thời gian";
        public static final String GENDER = "Giới tính";
        public static final String TOKEN = "Khoá";
        public static final String ACCESS_TOKEN = "Access token";
        public static final String REFRESH_TOKEN = "Refresh token";
    }

    public static class NOTIFICATION{
        public static final String CANCELED_BY_SYSTEM = " đã bị huỷ bởi hệ thống";
        public static final String WELCOME_TITLE = "Chào mừng";
        public static final String WELCOME_MESSAGE0 = "Chào mừng người dùng ";
        public static final String WELCOME_MESSAGE1 = " đến với bách hoá xanh, hãy trải nghiệm và cho chúng tôi những đánh giá tích cực :D";
    }
}
