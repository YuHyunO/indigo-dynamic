@value_mapper(id=IF_001-mapper-1, createNullKey=true, nullToEmptyString=true){
    #KUNUR -> f!String.substring(KUNUR, 1, 3);
    #AFSDE -> f!String.substring(KUNUR, 1, 3);
    #EWSX -> f!EWSX.toUpperCase();
    #VDSS -> f!String.substring(KUNUR, 1, 3);

}

@key_mapper(id=IF_001-mapper-1, createNullKey=true){//
  #KUNUR -> TKUNTN

}