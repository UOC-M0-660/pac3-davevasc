# PARTE TEORICA

### Lifecycle

#### Explica el ciclo de vida de una Activity.

##### ¿Por qué vinculamos las tareas de red a los componentes UI de la aplicación?
Las tareas de red tardan un tiempo significativamente mayor al tiempo que tarda la interfaz de nuestra aplicación, por eso, todas las llamadas al servidor las realizamos en segundo plano, en este caso, con Coroutines, para que no bloqueen el hilo principal de la aplicación, y de esa forma, no perjudique la experiencia de usuario. Desde la UI activamos la necesidad de cargar los Streams de Twitch, entonces, lanzamos una coroutina que ejecutará en segundo plano la petición al servidor, pero, mientras esto ocurre, el hilo principal sigue en ejecución, es decir que la aplicación no queda bloqueada, de ea forma separamos la UI con las peticiones de red, para que no afecten a la experiencia de usuario ni bloqueen la aplicación. Una vez que obtenemos la respuesta del servidor, la coroutina continúa y carga los datos en hilo principal es decir en la UI, sin que la aplicación quede bloqueada durante la carga. Incluso aislamos y capturamos por separado los posibles fallos que podamos obtener de las peticiones de red, para que no interfieran de ninguna manera en la UI. Por lo tanto, las tareas de red y la UI las mantenemos separadas para que no interfieran en la experiencia de usuario pero al mismo tiempo están vinculadas para que una vez que obtengamos la respuesta del servidor, se cargan correctamente en la UI sin dejar la pantalla bloqueada al usuario.

##### ¿Qué pasaría si intentamos actualizar la recyclerview con nuevos streams después de que el usuario haya cerrado la aplicación?
Si, por ejemplo, lanzamos la petición de obtener los Streams mediante la API, y finalizamos / destruimos (finish()) la Activity justo antes de que el clienteHttp nos haya devuelto la respuesta vía API, entonces el método getStreams nos devolverá el siguiente error: “Job was cancelled”, porque la coroutina lanzada ha sido cancelada antes de que haya finalizado, es decir, antes de que hayamos recibido la respuesta.
Si por el contrario, finalizamos / destruimos (finish()) la Activity después de haber obtenido correctamente la respuesta de la API y justo antes de cargar la recyclerview, entonces el método seguirá porque ya hemos obtenido la respuesta y el código está corriendo en nuestra aplicación (ya no está a la espera de la respuesta de la API), así que la recyclerview será cargada con los streams correctamente pero justo cuando termine ese proceso de carga (justo en el instante en el que ha terminado de cargar en la recyclerview el último elemento), la Aplicación terminará y quedará cerrada. No finalizará con error, porque la habrá cerrado el usuario manualmente o la habremos inducido el cierre directamente con un finish().

##### Describe brevemente los principales estados del ciclo de vida de una Activity.
1. En ejecución: La Activity está en primer plano y el usuario puede interactuar con ella. Para llegar a este estado, la Activity ejecuta los siguientes 3 métodos de su ciclo de vida desde su creación:
onCreate() Cuando se lanza la actividad y donde se inicializan las principales funciones.
onStart() Justo después de onCreate() y es el paso previo a ser visualizada en pantalla.
onResume() Justo después de onStart(), aquí la interfaz ya es visible y se puede interactuar con sus elementos.
2. Pausada: La actividad ha perdido el foco o deja de estar en primer plano. Se ejecuta el método:
onPause() Por ejemplo, después de pulsar el botón de aplicaciones recientes.
3. Detenida: La actividad deja de ser visible para el usuario. Se ejecuta el método:
onStop() Por ejemplo, cuando lanzamos una nueva Activity.
onRestart() Opcionalmente, se lanza después de onStop(), solo en caso de que el usuario decida retomar nuevamente la actividad al primer plano.
4. Finalizada o muerta: La aplicación ha finalizado o ha sido destruida. Se ejecuta el método:
onDestroy() Por ejemplo cuando el usuario ha decidido finalizar la Activity.

---

### Paginación 

#### Explica el uso de paginación en la API de Twitch.

##### ¿Qué ventajas ofrece la paginación a la aplicación?
La paginación permite cargar de forma rápida y ágil los primeros elementos de una lista alojada en un servidor, la cual puede llegar a ser muy larga. Normalmente con mostrar los primeros X elementos de una lista, es suficiente para el usuario, véase resultados de Google, movimientos bancarios, etc. Como el cursor nos lo proporciona a través de la API en cada llamada, aunque hagan modificaciones de datos y páginas en el servidor, incluso aunque eliminen elementos de la lista, no tendremos que tocar nuestro código ni cambiar la API de llamada para adaptarnos. Las llamadas a la API siempre tardarán el mismo tiempo: El tiempo en obtener X elementos de la página 1 sería el mismo que obtener los X elementos de la página 800, por ejemplo. Hay que tener en cuenta también lo que puede ser una posible desventaja: como cada cursor nos lo va obteniendo en cada llamada, si queremos ir directamente por ejemplo a la página 100, tendríamos que obtener previamente los primeros 99 elementos (ya sea en una única o en varias consultas) para saber cual es el cursor que se inicia en la página 100.

##### ¿Qué problemas puede tener la aplicación si no se utiliza paginación?
En primer lugar sería la eficiencia: Cuando enviamos una petición a un servidor, éste la recibe y a su vez manda la consulta a su base de datos, la procesa y luego nos devuelve el resultado, lo cual, cuanto más grande sea el volumen de datos solicitado, más tardará en devolvérnoslo. Por lo tanto, si inicialmente pedimos todos los datos disponibles al servidor, tardaremos mucho tiempo en obtener los resultados y en caso de ser un volumen de datos demasiado grande, correremos el riesgo de que obtengamos un error por timeout que nos devolverá el cliente http. Otro problema sería la experiencia de usuario: Nada más abrir la aplicación, tendríamos que esperar hasta haber obtenido todos los Streams del servidor, lo cual podría dejarnos esperando varios minutos con la recyclerview vacía, cosa que seguro frustrará al usuario. Rendimiento: Además, tener cargada una lista demasiado grande desde un inicio y de forma constante, nos consumiría más recursos de nuestro teléfono y podría afectar al rendimiento de la aplicación.

##### Lista algunos ejemplos de aplicaciones que usan paginación.
Wallapop usa paginación en la lista principal de los resultados, a simple vista puede parecer que no usa este método, pero si nos fijamos bien, al llegar al último elemento cargado de la lista haciendo scroll, en vez de mostrar un botón para que podamos cargar más elementos, lo que hace es directamente cargar los siguientes X elementos de forma automática sin necesidad de que el usuario tenga que pulsar ningún botón, y así sucesivamente. Ese efecto de hacer la carga de forma transparente al usuario hace que parezca que inicialmente se han cargado todos los elementos, pero realmente se usa la paginación. Al actualizar la lista haciendo un swipe hacia abajo, se vuelve a recargar la lista y nos vuelve a mostrar solamente los X primeros artículos actualizados.
Otra aplicación que utiliza paginación es Forsquare, en la cual la implementan de la misma forma que en Wallapop, en los resultados de las búsquedas, cuando llegamos al último elemento de la lista haciendo scroll, la aplicación carga automáticamente los X siguientes elementos haciendo que el proceso sea transparente para el usuario. Si actualizamos la lista haciendo un swipe hacia abajo, al igual que en Wallapop se actualizará la lista y se cargarán los primeros X elementos actualizados.
Además de estas dos, tenemos también otras aplicaciones que hacen uso de la misma técnica de paginación como Groupon, El Tenedor o la mismísima Youtube.
