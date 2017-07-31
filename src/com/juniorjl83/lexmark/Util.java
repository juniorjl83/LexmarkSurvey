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

   public static ValidacionJson validateJsonStructure(String jsonPreguntas)
   {

      StringBuffer sb = new StringBuffer("");
      JSONParser parser = new JSONParser();
      ValidacionJson validacionJson = new ValidacionJson();
      List preguntas = new ArrayList();

      try
      {
         Object obj = parser.parse(jsonPreguntas);
         JSONArray objetos = (JSONArray) obj;

         Iterator iterator = objetos.iterator();
         int numeroPregunta = 1;

         while (iterator.hasNext())
         {
            preguntas.add(jsonObjtoPreguntaObjConverter(
                  (JSONObject) iterator.next(), numeroPregunta));
            numeroPregunta++;
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
               .setMsgValidacion("error en el formato de las preguntas");
         validacionJson.setError(true);
         return validacionJson;
      }

      validacionJson.setPreguntas(preguntas);
      return validacionJson;
   }

   private static Pregunta jsonObjtoPreguntaObjConverter(JSONObject jsonObject,
         int numeroPregunta) throws JSONException
   {

      Pregunta pregunta = new Pregunta();
      pregunta.setId(numeroPregunta);
      pregunta.setTipo((String) jsonObject.get("tipo"));
      pregunta.setPregunta((String) jsonObject.get("pregunta"));
      pregunta.setOpciones(getOpciones((JSONArray) jsonObject.get("opciones"),
            numeroPregunta));
      validarPregunta(pregunta);
      return pregunta;

   }

   private static List getOpciones(JSONArray jsonArray, int noPregunta)
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
         validarOpcion(opcion, noPregunta);
         opciones.add(opcion);
         numeroOpcion++;
      }
      return opciones;
   }

   private static void validarPregunta(Pregunta pregunta) throws JSONException
   {
      if (isEmpty(pregunta.getTipo()))
      {
         throw new JSONException(
               "Tipo en pregunta No: " + pregunta.getId() + " es obligatorio.");
      }
      else
      {
         if (!pregunta.getTipo().equals("ommr")
               && !pregunta.getTipo().equals("omur")
               && !pregunta.getTipo().equals("texto"))
         {
            throw new JSONException(
                  "Tipo no soportado, en pregunta No: " + pregunta.getId());
         }
      }
      if (isEmpty(pregunta.getPregunta()))
      {
         throw new JSONException("Pregunta en pregunta No: " + pregunta.getId()
               + " es obligatorio.");
      }
      if (pregunta.getTipo().equals("ommr")
            || pregunta.getTipo().equals("omur"))
      {
         if (pregunta.getOpciones().size() == 0)
         {
            throw new JSONException("Opciones en pregunta No: "
                  + pregunta.getId() + " es obligatorio cuando es de tipo: "
                  + pregunta.getTipo());
         }
      }
   }

   private static void validarOpcion(Opcion opcion, int noPregunta)
         throws JSONException
   {
      if (isEmpty(opcion.getDescripcion()))
      {
         throw new JSONException("Descripción en pregunta No: " + noPregunta
               + " opción No: " + opcion.getNumero() + " es obligatorio.");
      }
      if (isEmpty(opcion.getValor()))
      {
         throw new JSONException("Valor en pregunta No: " + noPregunta
               + " opción No: " + opcion.getNumero() + " es obligatorio.");
      }

   }

   public static Encuesta parseSettingToEncuestaObj(
         SettingDefinitionMap instance)
   {
      Encuesta encuesta = new Encuesta();
      SettingDefinition name = instance.get("settings.instanceName");
      SettingDefinition fechaInicio = instance.get("settings.instanceBegin");
      SettingDefinition fechaFin = instance.get("settings.instanceEnd");
      SettingDefinition json = instance.get("settings.instanceJson");

      ValidacionJson validacionJson = validateJsonStructure(
            (String) json.getCurrentValue());

      encuesta.setNombre((String) name.getCurrentValue());
      encuesta.setFechaInicio(
            stringToDate((String) fechaInicio.getCurrentValue()));
      encuesta.setFechaFin(stringToDate((String) fechaFin.getCurrentValue()));
      encuesta.setPreguntas(validacionJson.getPreguntas());

      return encuesta;
   }

   private static Date stringToDate(String fecha)
   {
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
