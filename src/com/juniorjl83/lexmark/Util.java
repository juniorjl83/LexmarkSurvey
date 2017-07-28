package com.juniorjl83.lexmark;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.parser.JSONParser;

public class Util
{
   private static final String CARRIAGE_RETURN = System
         .getProperty("line.separator");

   public static boolean isValidDate(String fecha)
   {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      try
      {
         formatter.parse(fecha);
      }
      catch (ParseException e)
      {
         return false;
      }
      return true;
   }

   public static boolean isValidJson(String json)
   {

      JSONParser parse = new JSONParser();
      try
      {
         parse.parse(json);
      }
      catch (org.json.simple.parser.ParseException e)
      {
         return false;
      }

      return true;
   }

   public static boolean isEmpty(String value)
   {
      if (value != null && value.length() > 0)
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public static String dateValidation(String beginDate, String endDate)
   {

      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      Date bDate, eDate;
      StringBuffer sb = new StringBuffer("");
      try
      {

         bDate = formatter.parse(beginDate);
         eDate = formatter.parse(endDate);
         Activator.getLog().info("date parsed begin :: " + bDate);
         Activator.getLog().info("date parsed end :: " + eDate);
         Activator.getLog().info("fecha maquina :: " + new Date());
         Activator.getLog().info("if begin < actual :: " + bDate.before(new Date()));
         
         if (bDate.before(new Date()))
         {
            sb.append(
                  "La fecha inicio no puede ser anterior o igual al día de hoy.");
            sb.append(CARRIAGE_RETURN);
         }

         if (eDate.before(new Date()))
         {
            sb.append(
                  "La fecha fin no puede ser anterior o igual al día de hoy.");
            sb.append(CARRIAGE_RETURN);
         }

         if (eDate.before(bDate) || eDate.equals(bDate))
         {
            sb.append(
                  "La fecha fin no puede ser anterior o igual a la fecha inicio.");
            sb.append(CARRIAGE_RETURN);
         }
      }
      catch (Exception e)
      {
         return "Error validando las fechas.";
      }

      return sb.toString();
   }
}
