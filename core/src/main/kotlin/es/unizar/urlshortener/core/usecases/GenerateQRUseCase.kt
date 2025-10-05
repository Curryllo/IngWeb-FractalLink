package es.unizar.urlshortener.core.usecases

import qrcode.QRCode
import svg.SVGGraphicsFactory
import java.util.Base64


/**
 * Interface for generating QR codes from URIs.
 */
interface GenerateQRUseCase {
    fun generate(url: String): String
}

/**
 * Implementation of [GenerateQRUseCase] using the `qrcode` library to generate SVG QR codes.
 *
 * @return A String containing the QR code as a base64-encoded SVG data URI.
 */
class GenerateQRUseCaseImpl : GenerateQRUseCase {
    override fun generate(url: String): String {
        val qrCode = QRCode.ofSquares()
            .withGraphicsFactory(SVGGraphicsFactory())
            .build(url)
        val svgBytes = qrCode.renderToBytes()
        val svgBase64 = Base64.getEncoder().encodeToString(svgBytes)
        val dataUri = "data:image/svg+xml;base64,$svgBase64"
        return dataUri
    }
}
