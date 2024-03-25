package data

interface GradeValue {
    companion object {
        fun fromString(value: String): GradeValue {
            val name = value
                .uppercase()
                .replace("+", "_PLUS")
                .replace("º", "A")
            return SportsGrade.entries.find { it.name.endsWith(name) }
                ?: ArtificialGrade.entries.find { it.name == name }
                ?: SportsGrade.UNKNOWN
        }
    }

    val name: String
}

enum class SportsGrade : GradeValue {
    G1,
    G2, G2_PLUS,
    G3A, G3B, G3C, G3_PLUS, G3,
    G4A, G4B, G4C, G4_PLUS, G4,
    G5A, G5B, G5C, G5_PLUS, G5,
    G6A, G6A_PLUS, G6B, G6B_PLUS, G6C, G6C_PLUS,
    G7A, G7A_PLUS, G7B, G7B_PLUS, G7C, G7C_PLUS,
    G8A, G8A_PLUS, G8B, G8B_PLUS, G8C, G8C_PLUS,
    G9A, G9A_PLUS, G9B, G9B_PLUS, G9C, G9C_PLUS,
    UNKNOWN
}

enum class ArtificialGrade : GradeValue {
    A0,
    A1, A1_PLUS,
    A2, A2_PLUS,
    A3, A3_PLUS,
    A4, A4_PLUS,
    A5, A5_PLUS
}
