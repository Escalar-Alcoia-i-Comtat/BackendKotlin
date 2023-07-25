package com.arnyminerz.escalaralcoiaicomtat.backend.database

object SqlConsts {
    /** The maximum length of a display name. */
    const val DISPLAY_NAME_LENGTH = 64

    /** The maximum length of a URL. */
    const val URL_LENGTH = 128

    /** The maximum length of a file name. */
    const val FILE_LENGTH = 128

    /**
     * Represents the length of the points array.
     *
     * This variable represents the length of the points array used in the application. The points array is an array
     * that stores the coordinates of points in a two-dimensional space. This variable is a constant and its value
     * cannot be changed during runtime.
     *
     * @since 1.0.0
     */
    const val POINTS_LENGTH = 4096

    const val GRADE_LENGTH = 4

    const val PITCH_INFO_LENGTH = 10240

    const val BUILDER_LENGTH = 4096
    const val RE_BUILDER_LENGTH = 4096

    const val DESCRIPTION_LENGTH = 10240
}
