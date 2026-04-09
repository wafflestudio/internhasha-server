package com.wafflestudio.internhasha.s3.dto

data class S3Files(
    var cvKey: String? = null,
    var profileImageKey: String? = null,
    var portfolioKey: String? = null,
    var companyInfoPDFKey: String? = null,
) {
    fun hasAnyFile() = profileImageKey != null || cvKey != null || portfolioKey != null || companyInfoPDFKey != null
}
