package im.kny

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Parameter
import org.hibernate.id.enhanced.SequenceStyleGenerator
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="person")
data class Person constructor(
    @Id
    @GeneratedValue(generator = "person_id_seq")
    @GenericGenerator(
        name = "person_id_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = [
            Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "person_id_seq"),
            Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
        ]
    )
    var id: Long?=null,
    var name: String
) {

}
