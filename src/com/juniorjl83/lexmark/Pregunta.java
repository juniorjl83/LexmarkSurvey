package com.juniorjl83.lexmark;

import java.util.List;

public class Pregunta
{
   private int id;
   private String tipo;
   private String pregunta;
   private List opciones;

   public Pregunta()
   {
      super();
   }

   public Pregunta(int id, String tipo, String pregunta, List opciones)
   {
      super();
      this.id = id;
      this.tipo = tipo;
      this.pregunta = pregunta;
      this.opciones = opciones;
   }

   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   public String getTipo()
   {
      return tipo;
   }

   public void setTipo(String tipo)
   {
      this.tipo = tipo;
   }

   public List getOpciones()
   {
      return opciones;
   }

   public void setOpciones(List opciones)
   {
      this.opciones = opciones;
   }

   public String getPregunta()
   {
      return pregunta;
   }

   public void setPregunta(String pregunta)
   {
      this.pregunta = pregunta;
   }

   public String toString()
   {
      return "Pregunta [id=" + id + ", tipo=" + tipo + ", pregunta=" + pregunta
            + ", opciones=" + opciones + "]";
   }

}
