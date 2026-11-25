# Resumen de cambios

Chicos ya hice mi desmadre

---
## 0. Cambios de directorio

Separe todo en paquetes independientes, para tener el espacio un poquitito mas limpio.
Este estilo de arquitectura me sienta bien, pero no me aguito mucho si lo cambian.

Aun no hago tests, pero cuando los haga los voy a ir colocando en la carpeta dedicada.

---

## 1. Cambios del AST

La risa, se separa completamente el AST de la arquitectura de Nodos como clases dedicadas, en su lugar, se definen como records para dar inmutabilidad al tipo.

Cuando se ve en detalle, muchos nodos realmente no hacian nada (NodoDeclList, NodoStmtList, por decir algunos).
Entonces en lugar de meter al arbol de estructura un monton de nodos que no hacen nada, reducimos los elementos que realmente si empleamos y que tienen relevancia semantica.

### Y como jala oq

En el paquete ast tenemos tres archivos:
* Expresion.java
* Sentencia.java
* Evaluador.java

### Expresion
Este archivo define los tipos de expresiones que existen, **solo las que resuelven en un valor**, no estatutos/sentencias completas.

Se pueden ver macizo los paralelismos con las categorias lexicas de hecho.

### Sentencia
Es el complemento del anterior, aqui se define los tipos de sentencias (el codigo ejecutable) q puede procesar el lenguaje. Si se mete alguna cosa extra, aqui mero se resuelve. Aqui es donde viven las reglas de gramatica.ue 

Entonces, ya no tenemos NodoStatementList, si no que al procesarse, tenemos un List<Statement> como objeto de java, y se acabo. La estructura de arbol sigue en pie perfectamente, solo que ahora implementado como listas iterables, y no arboles que requieren exploracion recursiva.

### Evaluador
Es un monolito gigantesco que implementa todas las reglas que tenian los metodos evaluar() antiguos.
Implementacion del patron visitor porque creo que es la unica palabra q escuche mas en esa clase por debajo de "no falten" o algo asi.

Oigan esta sintaxis de switch esta bien chida en la uni nos estan ensenando java de 1997.

---

## 2. Separacion de numeros y temas de booleans

Los tipos de datos numericos son importantes pq tenemos que definir los tamanos de la memoria q queremos. Y pues si unificamos todo en un mismo tipo (real/float) pues esta dificil saber cual es int y cual real, mucho menos saber cual es la memoria que debemos reservar.

* Meti true y false en las palabras reservadas del lexer, a no ser que el plan fuera hacer conversion implicita, en cuyo caso pues se arregla aparte jejeje

* En el parser ya hay trazabilidad de tipos desde el momento de la construccion del arbol asi q todo cool.

* Con el tema de operaciones, mezclar ints con dobles promueve el int a doble, la division siempre genera dobles, no estoy considerando division entera u operador % o similares.

---

## 3. Analisis semantico del arbol

Entonces ahora si hay un paso dedicado a mirar el arbol y ver que no me haya montado zurres.

* Detecta variables duplicadas y uso de variables no declaradas.
* Valida tipos de datos en operaciones
  * Aritméticos y relacionales piden numeros (INTEGER o REAL).
  * Lógicos (AND, OR) y condiciones de IF/WHILE piden BOOLEAN.
  * Igualdad (=, <>) validan compatibilidad entre valores.
* Asignaciones exactas y promoción implícita  de INTEGER a REAL, bloqueando asignaciones incompatibles (REAL -> INTEGER o BOOLEAN -> INTEGER).

---

## 4. Tabla de Simbolos enchonchada

Le di de comer crico a la tabla de simbolos para guardar informacion adicional:

* **`value`**: Almacena el valor durante la ejecucion.
* **`usada`**: Registra si la variable fue leída en alguna expresión, habilitando la detección/rechazo de **variables declaradas sin uso**.
* **`escrituras`**: Contabiliza cuántas veces se modifica la variable.
* **`esConstanteCompilacion` y `valorConstante`**: Identifica variables que reciben una única asignación constante, habilitando el **plegado y propagación de constantes** así como la **eliminación de código muerto** en ramas estáticas.

Esta informacion extra es preparacion para las tecnicas de optimizacion q tengo pensadaS:
* Salto de codigo muerto
* Plegado y propagacion de constantes
* Rechazo de variables sin uso
* Salto de escrituras muertas

Pero bueno eso ya despues jejej

---

### Ypos ya
Ahi te mando gio un archivo .txt con todos los codigos .java metidos, ya para q nomas se lo den a la IA y puedan revisar o pedirle cosas.

Ay nos vemos los tkm

## Actualización 24 de noviembre
Se agrego la carpeta codegen en la cual se encuentra Cuadruplos donde se maneja mediante el uso de la estructura de record para mantener en uso la misma estructura de los anteriorees codigos
En GeneradorCuadruplo es como funcionara el codigo para poder aumemtar los temporales y como es que los genera
Falta implementar el codigo que genera el ASM osea el ensamblador que creo que ira para x86

## Generador de ASM colocado
No se agrego lo del read y write porque era muchisimos lineas de codigo en pura impresion de las 300 se mandaba a 700-800 lineas de pura impresion
Faltaria hacer mas archivos de prueba porque el que se llama Fuente.pas me dio algun error en la parte de declaracion de variables.