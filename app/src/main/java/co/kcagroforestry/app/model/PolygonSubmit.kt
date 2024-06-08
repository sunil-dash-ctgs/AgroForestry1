package co.kcagroforestry.app.model

data class PolygonSubmit(
    val farmeruniquid: String,
    val ranges: ArrayList<LocationModel>,
    val plot_area: String,
    val plot_id: String,

)
