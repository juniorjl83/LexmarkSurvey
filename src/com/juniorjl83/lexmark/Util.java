package com.juniorjl83.lexmark;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.lexmark.prtapp.settings.SettingDefinition;
import com.lexmark.prtapp.settings.SettingDefinitionMap;
import com.lexmark.prtapp.util.AppLogRef;
import com.lexmark.prtapp.util.Messages;

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
         Activator.getLog()
               .info("if begin < actual :: " + bDate.before(new Date()));

         /*if (bDate.before(new Date()))
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
         }*/

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

   public static ValidacionJson validateJsonStructure(String jsonEncuestas)
   {

      StringBuffer sb = new StringBuffer("");
      JSONParser parser = new JSONParser();
      ValidacionJson validacionJson = new ValidacionJson();
      List encuestas = new ArrayList();

      try
      {
         Object objEncuestas = parser.parse(jsonEncuestas);
         JSONArray arrayEncuestas = (JSONArray) objEncuestas;

         Iterator iteEncuestas = arrayEncuestas.iterator();
         int numeroEncuesta = 1;

         while (iteEncuestas.hasNext())
         {
            encuestas.add(jsonObjtoEncuestaObjConverter(
                  (JSONObject) iteEncuestas.next(), String.valueOf(numeroEncuesta)));
            numeroEncuesta++;
         }
      }
      catch (JSONException e)
      {
         validacionJson.setMsgValidacion(e.getMessage());
         validacionJson.setError(true);
         return validacionJson;
      }
      catch (org.json.simple.parser.ParseException e)
      {
         validacionJson
               .setMsgValidacion("error en el formato de las encuestas");
         validacionJson.setError(true);
         return validacionJson;
      }

      validacionJson.setEncuestas(encuestas);
      return validacionJson;
   }

   private static Pregunta jsonObjtoPreguntaObjConverter(JSONObject jsonObject,
         int numeroPregunta, String noEncuesta) throws JSONException
   {

      Pregunta pregunta = new Pregunta();
      pregunta.setId(numeroPregunta);
      pregunta.setTipo((String) jsonObject.get("tipo"));
      pregunta.setPregunta((String) jsonObject.get("pregunta"));
      pregunta.setOpciones(getOpciones((JSONArray) jsonObject.get("opciones"),
            numeroPregunta, noEncuesta));
      validarPregunta(pregunta, noEncuesta);
      return pregunta;

   }
   
   private static Encuesta jsonObjtoEncuestaObjConverter(JSONObject jsonObject,
         String numeroEncuesta) throws JSONException
   {
      String msg = validarFechasEncuesta(jsonObject, numeroEncuesta);
      if (!isEmpty(msg)){
         throw new JSONException(msg);
      }
         
      Encuesta encuesta = new Encuesta();
      encuesta.setId(numeroEncuesta);
      encuesta.setNombre((String) jsonObject.get("nombre"));
      encuesta.setFechaInicio(stringToDate((String) jsonObject.get("inicio")));
      encuesta.setFechaFin(stringToDate((String) jsonObject.get("fin")));

      JSONArray objPreguntas = (JSONArray) jsonObject.get("preguntas");
      List preguntas = new ArrayList();
      Iterator iterator = objPreguntas.iterator();
      int numeroPregunta = 1;

      while (iterator.hasNext())
      {
         preguntas.add(jsonObjtoPreguntaObjConverter(
               (JSONObject) iterator.next(), numeroPregunta, numeroEncuesta));
         numeroPregunta++;
      }
      
      validarEncuesta(encuesta);
      
      return encuesta;

   }

   private static String validarFechasEncuesta(JSONObject jsonObject, String numeroEncuesta)
   {
      boolean isValidBeginDate = false;
      boolean isValidEndDate = false;
      String msg = "";
      
      String beginDate = (String) jsonObject.get("inicio");
      Activator.getLog()
            .info("begin date validation: " + Util.isValidDate(beginDate));
      if (!Util.isEmpty(beginDate))
      {
         if (!Util.isValidDate(beginDate))
         {
            Activator.getLog().info("entra if beginDate ");
            msg += "Error en el formato de la fecha inicio en la encuesta No: " + numeroEncuesta;
            msg += CARRIAGE_RETURN;
         }
         else
         {
            Activator.getLog().info("entra else beginDate ");
            isValidBeginDate = true;
         }
      }

      String endDate = (String) jsonObject.get("fin");
      Activator.getLog()
            .info("end date validation: " + Util.isValidDate(endDate));
      if (!Util.isEmpty(endDate))
      {
         if (!Util.isValidDate(endDate))
         {
            Activator.getLog().info("entra if endDate ");
            msg += "Error en el formato de la fecha fin en la encuesta No: " + numeroEncuesta;
            msg += CARRIAGE_RETURN;
         }
         else
         {
            Activator.getLog().info("entra else endDate ");
            isValidEndDate = true;
         }
      }

      if ((Util.isEmpty(beginDate) && !Util.isEmpty(endDate))
            || (!Util.isEmpty(beginDate) && Util.isEmpty(endDate)))
      {
         Activator.getLog().info("entra if bothDates ");
         msg += "Deben estar diligenciadas ambas fechas en la encuesta No: " + numeroEncuesta;
         msg += CARRIAGE_RETURN;
      }

      if (isValidBeginDate && isValidEndDate)
      {
         Activator.getLog().info("entra if both dates valid ");
         String dateValidation = Util.dateValidation(beginDate, endDate);
         if (!Util.isEmpty(dateValidation))
         {
            msg += dateValidation;
         }
      }
      return msg;
   }

   private static List getOpciones(JSONArray jsonArray, int noPregunta, String noEncuesta)
         throws JSONException
   {
      Iterator iterator = jsonArray.iterator();
      List opciones = new ArrayList();
      int numeroOpcion = 1;
      while (iterator.hasNext())
      {
         JSONObject jsonObject = (JSONObject) iterator.next();
         Opcion opcion = new Opcion();
         opcion.setNumero(numeroOpcion);
         opcion.setDescripcion((String) jsonObject.get("desc"));
         opcion.setValor((String) jsonObject.get("valor"));
         validarOpcion(opcion, noPregunta, noEncuesta);
         opciones.add(opcion);
         numeroOpcion++;
      }
      return opciones;
   }
   
   private static void validarPregunta(Pregunta pregunta, String numeroEncuesta) throws JSONException
   {
      if (isEmpty(pregunta.getTipo()))
      {
         throw new JSONException(
               "Tipo en pregunta No: " + pregunta.getId() + " es obligatorio en encuesta No: "
                     + numeroEncuesta);
      }
      else
      {
         if (!pregunta.getTipo().equals("ommr")
               && !pregunta.getTipo().equals("omur")
               && !pregunta.getTipo().equals("texto")
               && !pregunta.getTipo().equals("like")
               && !pregunta.getTipo().equals("numerico"))
         {
            throw new JSONException(
                  "Tipo no soportado, en pregunta No: " + pregunta.getId() 
                  + " es obligatorio en encuesta No: " + numeroEncuesta);
         }
      }
      if (isEmpty(pregunta.getPregunta()))
      {
         throw new JSONException("Pregunta en pregunta No: " + pregunta.getId()
               + " es obligatorio en encuesta No: "
                     + numeroEncuesta);
      }
      if (pregunta.getTipo().equals("ommr")
            || pregunta.getTipo().equals("omur"))
      {
         if (pregunta.getOpciones().size() == 0)
         {
            throw new JSONException("Opciones en pregunta No: "
                  + pregunta.getId() + " es obligatorio cuando es de tipo: "
                  + pregunta.getTipo() + " en encuesta No: "
                  + numeroEncuesta);
         }
      }
   }

   private static void validarEncuesta(Encuesta encuesta) throws JSONException
   {
      if (isEmpty(encuesta.getNombre()))
      {
         throw new JSONException(
               "Nombre de encuesta No: " + encuesta.getId() + " es obligatorio.");
      }
      if (encuesta.getPreguntas().size() == 0){
         throw new JSONException("Preguntas en encuesta No: "
               + encuesta.getId() + " es obligatorio.");
      }
         
   }
   
   private static void validarOpcion(Opcion opcion, int noPregunta, String noEncuesta)
         throws JSONException
   {
      if (isEmpty(opcion.getDescripcion()))
      {
         throw new JSONException("Descripción en opción No: " + opcion.getNumero()
               + " pregunta No: " + noPregunta + " y encuesta No: "
               + noEncuesta + " es obligatorio.");
      }
      if (isEmpty(opcion.getValor()))
      {
         throw new JSONException("Valor en opción No: " + opcion.getNumero()
               + " pregunta No: " + noPregunta + " y encuesta No: "
               + noEncuesta + " es obligatorio.");
      }

   }

   public static Servicio parseSettingToServicioObj(
         SettingDefinitionMap instance)
   {
      Servicio servicio = new Servicio();
      SettingDefinition name = instance.get("settings.instanceName");
      SettingDefinition json = instance.get("settings.instanceJson");

      ValidacionJson validacionJson = validateJsonStructure(
            (String) json.getCurrentValue());

      servicio.setNombre((String) name.getCurrentValue());
      servicio.setEncuestas(validacionJson.getEncuestas());

      return servicio;
   }

   private static Date stringToDate(String fecha)
   {
      if (isEmpty(fecha)){
         return null;
      }
         
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      try
      {
         return formatter.parse(fecha);
      }
      catch (ParseException e)
      {
         return null;
      }
   }
}
