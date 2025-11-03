
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

suspend fun sendEmailWithQR(
    to: String,
    subject: String,
    clientName: String,
    barberName: String,
    date: String,
    time: String,
    qrBitmap: Bitmap,
    fromEmail: String,
    fromPassword: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(fromEmail, fromPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(fromEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)

                // Crear el contenido multipart
                val multipart = MimeMultipart("related")

                // Parte 1: HTML
                val htmlPart = MimeBodyPart().apply {
                    val htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        </head>
                        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;">
                            
                            <!-- Contenedor Principal -->
                            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                                
                                <!-- Header con Gradiente Azul -->
                                <div style="background: linear-gradient(135deg, #1E3A8A 0%, #3B82F6 100%); padding: 40px 30px; text-align: center; border-radius: 0;">
                                    <h1 style="color: #ffffff; margin: 0; font-size: 32px; font-weight: bold; letter-spacing: 1px;">
                                        ✂️ FadeBarber
                                    </h1>
                                    <p style="color: rgba(255, 255, 255, 0.9); margin: 10px 0 0 0; font-size: 16px; font-weight: 500;">
                                        Tu cita ha sido agendada
                                    </p>
                                </div>
                                
                                <!-- Contenido Principal -->
                                <div style="padding: 40px 30px;">
                                    
                                    <!-- Saludo -->
                                    <h2 style="color: #1E293B; margin: 0 0 10px 0; font-size: 24px; font-weight: bold;">
                                        ¡Hola $clientName! 👋
                                    </h2>
                                    <p style="color: #64748B; line-height: 1.6; font-size: 16px; margin: 0 0 30px 0;">
                                        Nos complace confirmar que tu cita ha sido agendada exitosamente. Te esperamos en la fecha y hora indicadas.
                                    </p>
                                    
                                    <!-- Card de Detalles de la Cita -->
                                    <div style="background: linear-gradient(135deg, rgba(30, 58, 138, 0.05) 0%, rgba(59, 130, 246, 0.05) 100%); border-left: 4px solid #3B82F6; border-radius: 12px; padding: 25px; margin-bottom: 30px;">
                                        <h3 style="color: #1E3A8A; margin: 0 0 20px 0; font-size: 18px; font-weight: bold;">
                                            📋 Detalles de tu Cita
                                        </h3>
                                        
                                        <!-- Fecha -->
                                        <div style="display: flex; align-items: center; margin-bottom: 15px;">
                                            <div style="background-color: rgba(59, 130, 246, 0.1); border-radius: 10px; padding: 12px; margin-right: 15px; display: inline-block;">
                                                <span style="font-size: 24px;">📅</span>
                                            </div>
                                            <div>
                                                <p style="margin: 0; color: #64748B; font-size: 13px; font-weight: 500;">Fecha</p>
                                                <p style="margin: 5px 0 0 0; color: #1E293B; font-size: 16px; font-weight: bold;">$date</p>
                                            </div>
                                        </div>
                                        
                                        <!-- Hora -->
                                        <div style="display: flex; align-items: center; margin-bottom: 15px;">
                                            <div style="background-color: rgba(59, 130, 246, 0.1); border-radius: 10px; padding: 12px; margin-right: 15px; display: inline-block;">
                                                <span style="font-size: 24px;">🕐</span>
                                            </div>
                                            <div>
                                                <p style="margin: 0; color: #64748B; font-size: 13px; font-weight: 500;">Hora</p>
                                                <p style="margin: 5px 0 0 0; color: #1E293B; font-size: 16px; font-weight: bold;">$time</p>
                                            </div>
                                        </div>
                                        
                                        <!-- Barbero (opcional) -->
                                        <div style="display: flex; align-items: center;">
                                            <div style="background-color: rgba(59, 130, 246, 0.1); border-radius: 10px; padding: 12px; margin-right: 15px; display: inline-block;">
                                                <span style="font-size: 24px;">👤</span>
                                            </div>
                                            <div>
                                                <p style="margin: 0; color: #64748B; font-size: 13px; font-weight: 500;">Barbero</p>
                                                <p style="margin: 5px 0 0 0; color: #1E293B; font-size: 16px; font-weight: bold;">$barberName</p>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <!-- Sección del QR Code -->
                                    <div style="background-color: #ffffff; border: 2px solid #E2E8F0; border-radius: 16px; padding: 30px; text-align: center; margin-bottom: 30px;">
                                        <h3 style="color: #1E293B; margin: 0 0 15px 0; font-size: 18px; font-weight: bold;">
                                            Tu Código QR
                                        </h3>
                                        <p style="color: #64748B; font-size: 14px; margin: 0 0 20px 0;">
                                            Presenta este código al llegar a tu cita
                                        </p>
                                        
                                        <!-- QR Code con sombra y borde -->
                                        <div style="display: inline-block; background-color: #ffffff; padding: 20px; border-radius: 16px; box-shadow: 0 4px 20px rgba(30, 58, 138, 0.15);">
                                            <img src="cid:qrcode" alt="QR Cita" style="width: 220px; height: 220px; border: 3px solid #3B82F6; border-radius: 12px; display: block;" />
                                        </div>
                                    </div>
                                    
                                    <!-- Recordatorio Importante -->
                                    <div style="background-color: #FFF9E6; border-left: 4px solid #F59E0B; border-radius: 12px; padding: 20px; margin-bottom: 30px;">
                                        <div style="display: flex; align-items: flex-start;">
                                            <span style="font-size: 24px; margin-right: 15px;">⚠️</span>
                                            <div>
                                                <p style="margin: 0; color: #92400E; font-size: 14px; font-weight: bold;">
                                                    Importante
                                                </p>
                                                <p style="margin: 8px 0 0 0; color: #78350F; font-size: 13px; line-height: 1.5;">
                                                    • Por favor llega 5 - 10 minutos antes de tu cita<br>
                                                    • Trae tu código QR<br>
                                                    • Si necesitas cancelar, contáctanos con anticipación
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <!-- Botón de Acción -->
                                    <div style="text-align: center; margin-bottom: 30px;">
                                        <a href="tel:+523121234567" style="display: inline-block; background: linear-gradient(135deg, #1E3A8A 0%, #3B82F6 100%); color: #ffffff; text-decoration: none; padding: 16px 40px; border-radius: 12px; font-size: 16px; font-weight: bold; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);">
                                            Llamar a FadeBarber
                                        </a>
                                    </div>
                                    
                                </div>
                                
                                <!-- Footer -->
                                <div style="background-color: #F8F9FA; padding: 30px; text-align: center; border-top: 1px solid #E2E8F0;">
                                    <div style="margin-bottom: 15px;">
                                        <p style="margin: 0 0 5px 0; color: #64748B; font-size: 14px; font-weight: 500;">
                                            ¿Necesitas ayuda?
                                        </p>
                                        <p style="margin: 0; color: #3B82F6; font-size: 14px;">
                                            <a href="mailto:contacto@fadebarber.com" style="color: #3B82F6; text-decoration: none;">contacto@fadebarber.com</a>
                                        </p>
                                    </div>
                                    
                                    <!-- Redes Sociales (opcional) -->
                                    <div style="margin: 20px 0;">
                                        <p style="margin: 0 0 10px 0; color: #64748B; font-size: 13px;">
                                            Síguenos en redes sociales
                                        </p>
                                        <!-- Aquí puedes agregar iconos de redes sociales -->
                                    </div>
                                    
                                    <p style="margin: 15px 0 0 0; color: #94A3B8; font-size: 12px;">
                                        © 2025 FadeBarber. Todos los derechos reservados.<br>
                                        Ciudad de Villa de Álvarez, Colima, México
                                    </p>
                                </div>
                                
                            </div>
                            
                        </body>
                        </html>
                    """.trimIndent()

                    setContent(htmlContent, "text/html; charset=utf-8")
                }

                // Parte 2: Imagen QR embebida
                val imagePart = MimeBodyPart().apply {
                    // Convertir Bitmap a ByteArray
                    val outputStream = java.io.ByteArrayOutputStream()
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val imageBytes = outputStream.toByteArray()

                    // Crear DataSource y DataHandler
                    val dataSource = ByteArrayDataSource(imageBytes, "image/png")
                    dataHandler = DataHandler(dataSource)

                    // ID único para referenciar en el HTML
                    setHeader("Content-ID", "<qrcode>")
                    disposition = MimeBodyPart.INLINE
                    fileName = "qrcode.png"
                }

                // Agregar ambas partes
                multipart.addBodyPart(htmlPart)
                multipart.addBodyPart(imagePart)

                // Establecer el contenido del mensaje
                setContent(multipart)
            }

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}