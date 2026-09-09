package com.example.data.model

data class ScheduleTemplate(
    val taskName: String,
    val frequencyType: String,
    val frequencyValue: Int,
    val conditionValue: String? = null,
    val estimatedCost: Double? = null,
    val notes: String? = null
)

data class AssetTemplate(
    val category: String,
    val defaultName: String,
    val iconName: String,
    val suggestedSchedules: List<ScheduleTemplate>
)

object HomeMaintenanceTemplates {
    val templates = listOf(
        AssetTemplate(
            category = "HVAC & Cooling",
            defaultName = "Bedroom AC",
            iconName = "ac_unit",
            suggestedSchedules = listOf(
                ScheduleTemplate("Filter Mesh Cleaning", "DAYS", 30, estimatedCost = 0.0, notes = "Rinse dust filters under running tap water"),
                ScheduleTemplate("General Servicing", "MONTHS", 6, estimatedCost = 500.0, notes = "Technician chemical wash and gas pressure check"),
                ScheduleTemplate("Deep Cleaning & Blower Wash", "YEARS", 1, estimatedCost = 1200.0, notes = "Indoor and outdoor coil pressure jet wash")
            )
        ),
        AssetTemplate(
            category = "Kitchen Appliances",
            defaultName = "Water Purifier (RO)",
            iconName = "water_drop",
            suggestedSchedules = listOf(
                ScheduleTemplate("Sediment & Pre-Carbon Filter Change", "MONTHS", 6, estimatedCost = 650.0, notes = "Replace spun filter and carbon block"),
                ScheduleTemplate("RO Membrane & TDS Check", "YEARS", 1, estimatedCost = 1800.0, notes = "Inspect membrane flow rate and output TDS")
            )
        ),
        AssetTemplate(
            category = "Kitchen Appliances",
            defaultName = "Kitchen Chimney",
            iconName = "kitchen",
            suggestedSchedules = listOf(
                ScheduleTemplate("Baffle Filter Degreasing", "DAYS", 30, estimatedCost = 0.0, notes = "Soak in hot detergent water for 30 minutes"),
                ScheduleTemplate("Chimney Duct & Motor Servicing", "YEARS", 1, estimatedCost = 800.0, notes = "Exhaust motor oiling and duct cleaning")
            )
        ),
        AssetTemplate(
            category = "Major Appliances",
            defaultName = "Washing Machine",
            iconName = "local_laundry_service",
            suggestedSchedules = listOf(
                ScheduleTemplate("Tub Clean Cycle (Descaling)", "DAYS", 30, estimatedCost = 150.0, notes = "Run 90°C wash with descaling powder"),
                ScheduleTemplate("Drain Pump Filter & Hose Inspection", "MONTHS", 3, estimatedCost = 0.0, notes = "Clear lint, hair, and coins from bottom flap")
            )
        ),
        AssetTemplate(
            category = "Major Appliances",
            defaultName = "Refrigerator",
            iconName = "kitchen",
            suggestedSchedules = listOf(
                ScheduleTemplate("Condenser Coil Vacuuming", "MONTHS", 6, estimatedCost = 0.0, notes = "Dust coils at bottom/rear to maintain cooling efficiency"),
                ScheduleTemplate("Door Gasket & Temperature Check", "MONTHS", 6, estimatedCost = 0.0, notes = "Clean rubber seal with warm soapy water")
            )
        ),
        AssetTemplate(
            category = "Vehicles",
            defaultName = "Car",
            iconName = "directions_car",
            suggestedSchedules = listOf(
                ScheduleTemplate("Engine Oil & Filter Service", "MONTHS", 6, conditionValue = "or 5,000 km", estimatedCost = 3500.0, notes = "Synthetic oil, oil filter, air filter replacement"),
                ScheduleTemplate("Tire Rotation & Alignment", "MONTHS", 6, conditionValue = "or 10,000 km", estimatedCost = 800.0, notes = "Front/rear tire cross rotation and wheel balancing"),
                ScheduleTemplate("Brake Pads & Fluid Inspection", "YEARS", 1, conditionValue = "or 15,000 km", estimatedCost = 1200.0, notes = "Inspect caliper pads thickness and brake fluid level")
            )
        ),
        AssetTemplate(
            category = "Plumbing & Water",
            defaultName = "Main Water Tank",
            iconName = "water",
            suggestedSchedules = listOf(
                ScheduleTemplate("Overhead Tank Deep Cleaning", "MONTHS", 6, estimatedCost = 1000.0, notes = "Drain sediment, scrub algae, and UV disinfection"),
                ScheduleTemplate("Float Valve & Overflow Pipe Check", "MONTHS", 3, estimatedCost = 0.0, notes = "Verify auto shutoff ball valve works properly")
            )
        ),
        AssetTemplate(
            category = "Electrical & Backup",
            defaultName = "Home Inverter & Battery",
            iconName = "bolt",
            suggestedSchedules = listOf(
                ScheduleTemplate("Battery Distilled Water Top-Up", "MONTHS", 2, estimatedCost = 120.0, notes = "Check tubular battery water level indicators"),
                ScheduleTemplate("Terminal Greasing & Voltage Inspection", "MONTHS", 6, estimatedCost = 0.0, notes = "Clean sulfation and apply petroleum jelly to terminals")
            )
        ),
        AssetTemplate(
            category = "Home Care & Safety",
            defaultName = "Pest Control",
            iconName = "pest_control",
            suggestedSchedules = listOf(
                ScheduleTemplate("Termite & Cockroach Household Treatment", "MONTHS", 6, estimatedCost = 1500.0, notes = "Gel baiting in kitchen and boundary spray"),
                ScheduleTemplate("Mosquito Net & Mesh Inspection", "MONTHS", 3, estimatedCost = 0.0, notes = "Inspect window wire meshes for tears")
            )
        ),
        AssetTemplate(
            category = "Home Care & Safety",
            defaultName = "Gas Stove & Cylinders",
            iconName = "fireplace",
            suggestedSchedules = listOf(
                ScheduleTemplate("Burner Jet Cleaning & Leak Check", "MONTHS", 3, estimatedCost = 0.0, notes = "Clear clogged brass burner holes and check regulator seal"),
                ScheduleTemplate("Rubber Gas Hose Replacement", "YEARS", 2, estimatedCost = 350.0, notes = "Replace LPG orange wire-reinforced safety hose")
            )
        )
    )

    val categories = listOf(
        "All",
        "HVAC & Cooling",
        "Kitchen Appliances",
        "Major Appliances",
        "Vehicles",
        "Plumbing & Water",
        "Electrical & Backup",
        "Home Care & Safety",
        "Garden & Outdoor",
        "General"
    )
}
