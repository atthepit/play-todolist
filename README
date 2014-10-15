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

---------------------------------------
### Borrar Tareas
Por último es posible borrar tareas realizando la siguiente petición:

```
DELETE  /tasks/:id
```
Si todo ha ido bien, la petición devolverá un estado HTTP **204 No Content**
