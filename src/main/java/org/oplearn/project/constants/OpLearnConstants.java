package org.oplearn.project.constants;

public class OpLearnConstants {
  private OpLearnConstants() {
  }

  public static class CommonConstants {
    private CommonConstants() {
    }

    public static final String ENCODING_UTF_8 = "UTF-8";
    public static final String LANGUAGE = "Accept-Language";
    public static final String DEFAULT_LANGUAGE = "vi";
    public static final String PARAM_KEYWORD = "keyword";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_CURSOR = "cursor";
    public static final String PARAM_ALL = "all";
    public static final String PERCENT = "%";
    public static final String MESSAGE_SOURCE = "classpath:i18n/messages";
    public static final String NOT_FOUND_MESSAGE = "Not found";
    public static final String BAD_REQUEST_MESSAGE = "Bad request";
    public static final String CONFLICT_MESSAGE = "Conflict occurred";
    public static final String BLANK_MESSAGE = "";
    public static final String SUCCESS_MESSAGE = "Success";
    public static final String CREATED_MESSAGE = "Created";
  }

  public static class AuditorConstant {
    private AuditorConstant() {
    }

    public static final String ANONYMOUS = "anonymousUser";
    public static final String SYSTEM = "SYSTEM";
  }

  public static class MessageException {
    private MessageException() {
    }

    public static final String DEFAULT_CODE_BAD_REQUEST = "org.oplearn.project.exception.base.BadRequestException";
    public static final String DEFAULT_CODE_CONFLICT = "org.oplearn.project.exception.base.ConflictException";
    public static final String DEFAULT_CODE_NOTFOUND = "org.oplearn.project.exception.base.NotFoundException";
    public static final String DEFAULT_CODE_SERVER_ERROR = "org.oplearn.project.exception.base.InternalServerError";
    public static final String DEFAULT_CODE_UNAUTHORIZED = "org.oplearn.project.exception.base.UnauthorizedException";
  }

  public static class AuthConstant {
    private AuthConstant() {
    }

    public static final String TYPE_TOKEN = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";
    public static final String ROLES_CLAIM = "roles";
    public static final String TOKEN_TYPE_CLAIM = "type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String[] WHITE_LIST = {
          "/swagger-ui.html",
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/actuator/health",
          "/actuator/info",
          "/sitemap.xml",
          "/sitemaps/**",
          "/prerender/**"
    };
    public static final String[] MATCHER_AUTH_PUBLIC_API = {
      "/api/v1/auth/login",
      "/api/v1/auth/register",
      "/api/v1/auth/verify-otp",
      "/api/v1/auth/forgot-password",
      "/api/v1/auth/reset-password",
      "/api/v1/auth/refresh",
      "/api/v1/auth/logout",
      "/api/v1/auth/login/google"
    };
    public static final String[] MATCHER_ADMIN_API = {"/api/v1/admin/**"};
  }

  public static class VariableConstant {
    private VariableConstant() {
    }

    public static final String SIZE_DEFAULT = "10";
    public static final String PAGE_DEFAULT = "0";
    public static final String IS_ALL_DEFAULT = "false";
    /** Trần page size cho mọi API list/filter — chặn fetch quá lớn gây lag. */
    public static final int MAX_PAGE_SIZE = 200;
    /** Trần CỨNG cho nhánh all=true (thay Pageable.unpaged) — chống DoS kéo cả bảng
     *  trong 1 request. Tổng số (amount) vẫn đúng nhờ count query riêng. */
    public static final int MAX_ALL_SIZE = 10000;
  }
}
