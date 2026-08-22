package org.oplearn.project.exception.base;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.BLANK_MESSAGE;
import static org.oplearn.project.exception.base.StatusConstants.FORBIDDEN;

/** 403 — đã xác thực nhưng không được phép thao tác (khác 401 chưa/không đăng nhập). */
public class ForbiddenException extends BaseException {
  public ForbiddenException(String code) {
    super(code, BLANK_MESSAGE, FORBIDDEN, null);
  }
}
