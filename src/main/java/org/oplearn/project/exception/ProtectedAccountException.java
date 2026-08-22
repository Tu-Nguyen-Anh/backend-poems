package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ForbiddenException;

/** Sửa/xoá tài khoản hệ thống được bảo vệ (admin/superadmin) bởi người không phải chính chủ. */
public class ProtectedAccountException extends ForbiddenException {
  public ProtectedAccountException() {
    super("org.oplearn.project.exception.ProtectedAccountException");
  }
}
