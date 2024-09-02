@value_mapper(id=IF_001-mapper-1, createNullKey=true, nullToEmptyString=true)
{
    #KUNUR -> f!String.substring(KUNUR, 1, 3);

}

@key_mapper(id=IF_001-mapper-1, createNullKey=true) {
  #KUNUR -> TKUNTN;

}