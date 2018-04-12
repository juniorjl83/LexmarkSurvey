package com.juniorjl83.lexmark;

import java.util.ArrayList;
import java.util.List;

public class ValidacionJson
{
   String msgValidacion;
   List preguntas = new ArrayList();
   boolean isError = false;
   
   public String getMsgValidacion()
   {
      return msgValidacion;
   }
   public void setMsgValidacion(String msgValidacion)
   {
      this.msgValidacion = msgValidacion;
   }
   public List getPreguntas()
   {
      return preguntas;
   }
   public void setPreguntas(List preguntas)
   {
      this.preguntas = preguntas;
   }
   public boolean isError()
   {
      return isError;
   }
   public void setError(boolean isError)
   {
      this.isError = isError;
   }
   
   
}
