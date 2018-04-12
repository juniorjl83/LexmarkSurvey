package com.juniorjl83.lexmark;

public class Comparator implements java.util.Comparator
{

   public int compare(Object o1, Object o2)
   {
      return ((Encuesta) o1).getNombre().compareTo(((Encuesta) o2).getNombre());
   }

}
