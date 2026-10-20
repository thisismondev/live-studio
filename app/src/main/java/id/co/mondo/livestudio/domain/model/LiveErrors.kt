package id.co.mondo.livestudio.domain.model

/** Akun Facebook belum memenuhi syarat siaran langsung (mode profesional, usia akun, pengikut, dsb). */
class AccountRequirementException(message: String? = null) : Exception(message)

/** Token Facebook tidak valid / kedaluwarsa sehingga pengguna perlu login ulang. */
class SessionExpiredException(message: String? = null) : Exception(message)

/** Kegagalan umum dari Facebook Live Video API. */
class LiveApiException(message: String? = null) : Exception(message)