# Play-TodoList

App de prueba en Play + Scala para la asignatura MADS, Universidad de Alicante.

Ya en Heroku:
  [Play-TodoList](http://play-todolist-mads.herokuapp.com/)

## How-to

### Tareas

Las tareas están definidas por un texto – *label* – un usuario – *user* – y una fecha opcional – *due_to*.
Para obtener una tarea podemos realizar una petición indicando el *id* de la tarea:

```
GET /tasks/:id
```

La petición devolverá un *Json* con la información de la tarea:

```json
{
    id: 1,
    label: "Hello world",
    user: "anonymous",
    due_to: "2014-10-15
}
```

Para devolver el *Json* hay que implementar cómo se parsea la tarea a *Json* mediante un Writes:
```scala
implicit val taskWrites = new Writes[Task] {
    def writes(task : Task) : JsValue = {
        var json = Json.obj(
            "id" -> task.id,
            "label" -> task.label,
            "user" -> task.user
        )
        
        if(!task.dueTo.isEmpty) {
            json = json ++ Json.obj("due_to" -> task.dueTo.get.toString)
        }
        
        return json
    }
}
```
La función crea un objeto *Json* con la información básica de la tarea (*id*, *label* y *user*) y si la tarea tiene una fecha de finalización se le añade posteriormente.

También es necesario un parseador para obtener los datos devueltos por la base de datos y convertirlos a un objeto *Task*

```scala
val parser : RowParser[Task] = {
    get[Long]("task.id") ~
    get[String]("task.label") ~
    get[String]("task.user_login") ~
    get[Option[Date]]("task.due_to") map {
        case id~label~user~dueTo => Task(id, label, user, dueTo)
    }
}
```
```scala
def find(id: Long) : Option[Task] = DB.withConnection { implicit c =>
    SQL("select * from task where id = {id}").on(
        'id -> id
    ).as(parser.singleOpt)
}
```
En el caso anterior, se parserarán los datos devueltos por la base de datos y se devolvera una instancia de **Option**[*Task*] para que en caso de no estar la tarea buscada en la base de dato devuelva **None**

---------------------------------------
#### Obtener todas las tareas
Puedes obtener una lista de todas las tareas:
```
GET     /tasks
```
```json
[
    {
        id: 1
        label: "Hello world",
        user: "anonymous",
        due_to: "2014-10-15"
    },
    {
        id: 2,
        label: "Hola mundo",
        user: "pedro"
    },
    {
        id: 3,
        label: "Bonjour tout le monde",
        user: "pedro"
        due_to: "2014-09-10"
    }
]
```
En este caso, con el parseador de *Json* anterior, se puede parsear directamente una lista de Tareas a *Json*. Pero para obtener la lista de tareas de la base datos hay que crear otro parseador:

```scala
val task = {
    get[Long]("id") ~ 
    get[String]("label") ~
    get[String]("user_login") ~
    get[Option[Date]]("task.due_to") map {
        case id~label~user~dueTo => Task(id, label, user, dueTo)
    }
}
```

```scala
def all() : List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task").as(task *)
}
```
---------------------------------------
#### Tareas de un usuario
Puedes realizar la siguiente petición para obtener una lista de las tareas filtradas por usuario:
```
GET     /:user/tasks
```
```scala
[
    {
        id: 2,
        label: "Hola mundo",
        user: "pedro"
    },
    {
        id: 3,
        label: "Bonjour tout le monde",
        user: "pedro"
        due_to: "2014-09-10"
    }
]
```

Podemos añadir parametros a una query para realizar el filtro por usuario:
```scala
def findByUser(user: String) : List[Task] = DB.withConnection { 
    implicit c =>
        SQL("select * from task where user_login = {user}").on(
            'user -> user
        ).as(task *)
}
```

---------------------------------------
### Filtro por fecha
Puedes obtener una lista con las tareas que ya han *caducado* realizando la siguiente petición:
```
GET     /:user/tasks/expired
```
```json
[
    {
        id: 3,
        label: "Bonjour tout le monde",
        user: "pedro"
        due_to: "2014-09-10"
    }
]
```
También puedes obtener las tareas que caducarán o caducaron en un rango de fechas concreto:
```
GET     /tasks/expires/:year/:month/:day
```
Por ejemplo, puedes obtener las tareas que caducan en 2014:
```
GET     /tasks/expires/2014
```
```json
[
    {
        id: 1
        label: "Hello world",
        user: "anonymous",
        due_to: "2014-10-15"
    },
    {
        id: 3,
        label: "Bonjour tout le monde",
        user: "pedro"
        due_to: "2014-09-10"
    }
]
```
Las tareas que caducan en septiembre de 2014:
```
GET     /tasks/expires/2014/09
```
```json
[
    {
        id: 3,
        label: "Bonjour tout le monde",
        user: "pedro"
        due_to: "2014-09-10"
    }
]
```
O las tareas que caducan en una fecha concreta:
```
GET     /tasks/expires/2014/10/15
```
```json
[
    {
        id: 1
        label: "Hello world",
        user: "anonymous",
        due_to: "2014-10-15"
    }
]
```

### Crear tareas
Puedes añadir una nueva tarea a un usuario haciendo una peticion **POST** a la siguiente ruta:
```
POST    /:user/tasks
```

También puedes crear una tarea anónima realizando la siguiente petición:
```
POST    /tasks
```
Si la acción se realiza correctamente se mostrará un *Json* con los datos de la tarea recién creada:
```json
{
    id: 4
    label: "Hallo welt",
    user: "anonymous",
    due_to: "2014-10-15"
}
```
El cuerpo de la peticion **POST** se recibe en el controlador y se parsea mediante un objeto *Form*. En nuestro caso para obtener el texto y la fecha, debemos crear un *Form* con los siguientes campos:
```scala
val taskForm = Form(
    tuple(
        "label" -> nonEmptyText,
        "dueTo" -> optional(date)
    )
)
```

De esta forma, en la acción **create** del controlador de tareas podemos obtener los datos fácilmente:
```
var label : String = taskForm._1
var dueTo : Option[Date] = taskForm._2
val id = Task.create(label, user, dueTo)
```
Como se puede ver en la última línea del código anterior, al crear la tarea en base de datos devolvemos el identificador de la tarea recién creada. Podemos hacer eso de la siguiente manera:
```scala
def create(label: String, user: String, dueTo: Option[Date]) : Long =  {
    DB.withConnection { implicit c =>
        SQL("insert into task (label, user_login, due_to) values ({label}, {user}, {dueTo})").on(
            'label -> label,
            'user  -> user,
            'dueTo -> dueTo
        ).executeInsert()
    } match {
        case Some(long) => long //The Primary Key
    }
}
```
### Borrar Tareas
Por último es posible borrar tareas realizando la siguiente petición:

```
DELETE  /tasks/:id
```
Si todo ha ido bien, la petición devolverá un estado HTTP **204 No Content**
