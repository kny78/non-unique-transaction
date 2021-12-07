package im.kny

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class MyDummy constructor(
    @Id
    var id:Long
){

}