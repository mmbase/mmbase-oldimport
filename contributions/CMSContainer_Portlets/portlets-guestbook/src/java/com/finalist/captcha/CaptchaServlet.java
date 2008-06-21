package com.finalist.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import com.octo.captcha.service.CaptchaServiceException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class CaptchaServlet extends HttpServlet {

   private static final long serialVersionUID = -8617461903432594139L;


   @Override
   public void init(ServletConfig servletConfig) throws ServletException {
      super.init(servletConfig);
   }


   @Override
   protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
         throws IOException {

      // the output stream to render the captcha image as jpeg into
      ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
      try {
         // get the session id that will identify the generated captcha.
         // the same id must be used to validate the response, the session id is
         // a good candidate!
         String captchaId = httpServletRequest.getSession().getId();
         // call the ImageCaptchaService getChallenge method
         BufferedImage challenge = CaptchaServiceSingleton.getInstance().getImageChallengeForID(captchaId,
               httpServletRequest.getLocale());

         // a jpeg encoder
         JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(jpegOutputStream);
         jpegEncoder.encode(challenge);
      }
      catch (IllegalArgumentException e) {
         httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
         return;
      }
      catch (CaptchaServiceException e) {
         httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         return;
      }

      byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

      // flush it in the response
      httpServletResponse.setHeader("Cache-Control", "no-store");
      httpServletResponse.setHeader("Pragma", "no-cache");
      httpServletResponse.setDateHeader("Expires", 0);
      httpServletResponse.setContentType("image/jpeg");
      ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
      responseOutputStream.write(captchaChallengeAsJpeg);
      responseOutputStream.flush();
      responseOutputStream.close();
   }

}
