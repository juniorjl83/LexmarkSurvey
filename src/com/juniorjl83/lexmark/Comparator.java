package com.juniorjl83.lexmark;

public class Comparator implements java.util.Comparator
{

   public int compare(Object o1, Object o2)
   {
      return ((Servicio) o1).getNombre().compareTo(((Servicio) o2).getNombre());
   }

}
