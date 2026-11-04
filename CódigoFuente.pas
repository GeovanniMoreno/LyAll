PROGRAM VolumenDeUnaEsfera;
VAR
    radio, volumen :REAL;
    aceptado :BOOLEAN;
BEGIN
    WRITE("Introduce un valor para el radio: ");
    READ(radio);
    aceptado := radio >= 0;
    IF aceptado THEN 
        BEGIN
            volumen := 3/4 * 3.1415964 * radio*radio*radio;
            WRITE("El valor del volumen es: ", volumen);
        END;
END.

