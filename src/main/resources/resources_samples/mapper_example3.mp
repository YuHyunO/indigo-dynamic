@value_mapper(id=IF_001-mapper-1, create_null_key=true, null_to_empty_string=true){
    #KUNUR -> f!String.substring(KUNUR, 1, 3);

}

@key_mapper(id=IF_001-mapper-1, create_null_key=true){
  #KUNUR -> TKUNTN
}