package com.juniorjl83.lexmark;

import java.util.ArrayList;
import java.util.List;

public class Log
{
   private String serial;
   private String date;
   private List respuestas;
   private String separator = "|";
   private List encabezado;

   public Log()
   {
      super();

      serial = "";
      date = "";
      respuestas = new ArrayList();
      encabezado = new ArrayList();
   }

   public Log(String serial, String date, List respuestas)
   {
      super();
      this.serial = serial;
      this.date = date;
      this.respuestas = respuestas;
   }

   public String getSerial()
   {
      return serial;
   }

   public void setSerial(String serial)
   {
      this.serial = serial;
   }

   public String getDate()
   {
      return date;
   }

   public void setDate(String date)
   {
      this.date = date;
   }

   public List getRespuestas()
   {
      return respuestas;
   }

   public void setRespuestas(List respuestas)
   {
      this.respuestas = respuestas;
   }

   public void setSeparator(String separator)
   {
      this.separator = separator;
   }
   
   public List getEncabezado()
   {
      return encabezado;
   }

   public String toString()
   {
      StringBuffer builder = new StringBuffer();
      builder.append(serial);
      builder.append(separator);
      builder.append(date);
      builder.append(separator);

      for (int i=0; i< respuestas.size(); i++){
         builder.append(respuestas.get(i));
         if (i < (respuestas.size() - 1)){
            builder.append(separator);   
         }
      }
      
      return builder.toString();
   }
   
   public String toStringEncabezado()
   {
      StringBuffer builder = new StringBuffer();

      for (int i=0; i< encabezado.size(); i++){
         builder.append(encabezado.get(i));
         if (i < (encabezado.size() - 1)){
            builder.append(separator);   
         }
      }
      
      return builder.toString();
   }   

}
