package com.arnyminerz.escalaralcoiaicomtat.backend.database

object SqlConsts {
    /** The maximum length of an identifier in InfoTable */
    const val INFO_ID_LENGTH = 32

    /** The maximum length of a value in InfoTable */
    const val INFO_VALUE_LENGTH = 128

    /** The maximum length of a display name. */
    const val DISPLAY_NAME_LENGTH = 64

    /** The maximum length of a URL. */
    const val URL_LENGTH = 128

    /** The maximum length of a file name. */
    const val FILE_LENGTH = 128

    const val WEIGHT_LENGTH = 4

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

    const val GRADE_LENGTH = 8

    const val ENDING_LENGTH = 16

    const val PITCH_INFO_LENGTH = 10240

    const val BUILDER_LENGTH = 4096
    const val RE_BUILDER_LENGTH = 4096

    const val DESCRIPTION_LENGTH = 10240

    /**
     * The maximum length of the images column in paths.
     *
     * Matches 10 files.
     */
    const val IMAGES_LENGTH = FILE_LENGTH * 10
}
