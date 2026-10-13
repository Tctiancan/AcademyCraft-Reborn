package org.academy.api.client.resources;

import net.minecraft.resources.ResourceLocation;

import static org.academy.AcademyCraft.academy;
import static org.academy.AcademyCraft.vanilla;

public final class R {
    private R() {
    }

    public static final class fonts {
        public static final ResourceLocation source_sans_3_regular = academy("fonts/source-sans-3-regular");
        public static final ResourceLocation wqy_microhei_modified = academy("fonts/wqy-microhei-modified");

        private fonts() {
        }
    }

    public static final class models {
        private models() {
        }

        public static final class block {
            public static final ResourceLocation wind_gen_base_transforms = academy("models/block/wind_gen_base_transforms");

            private block() {
            }
        }
    }

    public static final class particles {
        public static final ResourceLocation arc_medium = academy("particles/arc_medium");
        public static final ResourceLocation arc_small = academy("particles/arc_small");
        public static final ResourceLocation imag_phase_fluid = academy("particles/imag_phase_fluid");
        public static final ResourceLocation imag_phase_leaves = academy("particles/imag_phase_leaves");

        private particles() {
        }
    }

    public static final class shaders {
        public static final ResourceLocation position_tex = vanilla("core/position_tex");
        public static final ResourceLocation position_color = vanilla("core/position_color");
        public static final ResourceLocation position_tex_color = vanilla("core/position_tex_color");

        private shaders() {
        }

        public static final class core {
            public static final ResourceLocation vfx_particle = academy("core/vfx_particle");
            public static final ResourceLocation vfx_beam = academy("core/vfx_beam");
            public static final ResourceLocation vfx_ring = academy("core/vfx_ring");
            public static final ResourceLocation vfx_tex_billboard = academy("core/vfx_tex_billboard");
            public static final ResourceLocation vfx_sky_strike_quad = academy("core/vfx_sky_strike_quad");
            public static final ResourceLocation vfx_screen_flash = academy("core/vfx_screen_flash");
            public static final ResourceLocation vfx_lightning = academy("core/vfx_lightning");
            public static final ResourceLocation glow_blend = academy("core/glow_blend");
            public static final ResourceLocation distortion_ring = academy("core/distortion_ring");
            public static final ResourceLocation backdrop_sample = academy("core/backdrop_sample");
            public static final ResourceLocation gaussian_blur = academy("core/gaussian_blur");
            public static final ResourceLocation glow_circle = academy("core/glow_circle");
            public static final ResourceLocation platinum_cosmic_wing = academy("core/platinum_cosmic_wing");
            public static final ResourceLocation wing_ring_cosmic = academy("core/wing_ring_cosmic");
            public static final ResourceLocation vfx_model_mesh = academy("core/vfx_model_mesh");
            public static final ResourceLocation vfx_color_mesh = academy("core/vfx_color_mesh");
            public static final ResourceLocation vfx_block_mesh = academy("core/vfx_block_mesh");
            public static final ResourceLocation image = academy("core/image");
            public static final ResourceLocation image_circle = academy("core/image_circle");
            public static final ResourceLocation image_monochrome = academy("core/image_monochrome");
            public static final ResourceLocation imgui = academy("core/imgui");
            public static final ResourceLocation msdf_text = academy("core/msdf_text");
            public static final ResourceLocation msdf_text_instanced = academy("core/msdf_text_instanced");
            public static final ResourceLocation bitmap_text = academy("core/bitmap_text");
            public static final ResourceLocation particle_additive = academy("core/particle_additive");
            public static final ResourceLocation pos_color = academy("core/pos_color");
            public static final ResourceLocation rounded_rect = academy("core/rounded_rect");
            public static final ResourceLocation screen_blit = academy("core/screen_blit");
            public static final ResourceLocation spatial_cut_depth = academy("core/spatial_cut_depth");
            public static final ResourceLocation spatial_cut_mask = academy("core/spatial_cut_mask");
            public static final ResourceLocation spatial_cut_line = academy("core/spatial_cut_line");
            public static final ResourceLocation spatial_cut_glow = academy("core/spatial_cut_glow");
            public static final ResourceLocation spatial_cut_material = academy("core/spatial_cut_material");
            public static final ResourceLocation sdf_circle_glow = academy("core/sdf_circle_glow");
            public static final ResourceLocation sdf_sharp_margin = academy("core/sdf_sharp_margin");
            public static final ResourceLocation shockwave = academy("core/shockwave");
            public static final ResourceLocation skill_progress = academy("core/skill_progress");
            public static final ResourceLocation spatial_distortion = academy("core/spatial_distortion");
            public static final ResourceLocation spatial_cut = academy("core/spatial_cut");
            public static final ResourceLocation trail = academy("core/trail");
            public static final ResourceLocation vfxgraph_particle = academy("core/vfxgraph_particle");
            public static final ResourceLocation vfxgraph_fire = academy("core/vfxgraph_fire");
            public static final ResourceLocation vfxgraph_smoke = academy("core/vfxgraph_smoke");
            public static final ResourceLocation vfxgraph_mesh = academy("core/vfxgraph_mesh");
            public static final ResourceLocation vfxgraph_simple = academy("core/vfxgraph_simple");
            public static final ResourceLocation vfxgraph_arc = academy("core/vfxgraph_arc");
            public static final ResourceLocation vfxgraph_arc2 = academy("core/vfxgraph_arc2");
            public static final ResourceLocation vfxgraph_axial_burst = academy("core/vfxgraph_axial_burst");
            public static final ResourceLocation vfxgraph_sky_discharge = academy("core/vfxgraph_sky_discharge");
            public static final ResourceLocation vfxgraph_surface = academy("core/vfxgraph_surface");
            public static final ResourceLocation vfxgraph_cloud_vortex = academy("core/vfxgraph_cloud_vortex");
            public static final ResourceLocation vfxgraph_ink_storm = academy("core/vfxgraph_ink_storm");
            public static final ResourceLocation vfxgraph_iron_sand = academy("core/vfxgraph_iron_sand");
            public static final ResourceLocation vfxgraph_plasma_liquid = academy("core/vfxgraph_plasma_liquid");
            public static final ResourceLocation vfxgraph_plasma_halo = academy("core/vfxgraph_plasma_halo");
            public static final ResourceLocation vfxgraph_plasma_flash = academy("core/vfxgraph_plasma_flash");
            public static final ResourceLocation vfxgraph_plasma_spark = academy("core/vfxgraph_plasma_spark");
            public static final ResourceLocation vfxgraph_plasma_smoke = academy("core/vfxgraph_plasma_smoke");
            public static final ResourceLocation vfxgraph_impact_monochrome = academy("core/vfxgraph_impact_monochrome");
            public static final ResourceLocation vfxgraph_tornado_volume = academy("core/vfxgraph_tornado_volume");
            public static final ResourceLocation vfxgraph_tornado_dust = academy("core/vfxgraph_tornado_dust");
            public static final ResourceLocation vfxgraph_aeromanip_mist = academy("core/vfxgraph_aeromanip_mist");
            public static final ResourceLocation vfxgraph_aeromanip_blade = academy("core/vfxgraph_aeromanip_blade");
            public static final ResourceLocation vfxgraph_aeromanip_blade_mist = academy("core/vfxgraph_aeromanip_blade_mist");
            public static final ResourceLocation vfxgraph_aeromanip_blade_grain = academy("core/vfxgraph_aeromanip_blade_grain");
            public static final ResourceLocation vfxgraph_material_erosion = academy("core/vfxgraph_material_erosion");
            public static final ResourceLocation vfxgraph_material_smoke = academy("core/vfxgraph_material_smoke");
            public static final ResourceLocation vfxgraph_material_growth = academy("core/vfxgraph_material_growth");
            public static final ResourceLocation vfxgraph_material_light = academy("core/vfxgraph_material_light");
            public static final ResourceLocation vfxgraph_material_medium = academy("core/vfxgraph_material_medium");
            public static final ResourceLocation vfxgraph_material_contact = academy("core/vfxgraph_material_contact");
            public static final ResourceLocation vfxgraph_material_blade = academy("core/vfxgraph_material_blade");

            private core() {
            }
        }

        public static final class include {
            public static final ResourceLocation projection_utils = academy("include/projection_utils");

            private include() {
            }
        }
    }

    public static final class textures {
        public static final ResourceLocation ICON_AEROMANIP = academy("textures/ability/aeromanip/icon.png");
        public static final ResourceLocation ICON_DARKMATTER = academy("textures/ability/darkmatter/icon.png");
        public static final ResourceLocation SP_BAR_VALUE = academy("textures/hud/sp_bar_value.png");
        public static final ResourceLocation arc = academy("textures/ability/electromaster/skill/arc_generate/effect/line_segment.png");
        public static final ResourceLocation magnet_manipulation_icon = academy("textures/ability/electromaster/skill/magnet_manipulation/icon.png");
        public static final ResourceLocation mine_detect_icon = academy("textures/ability/electromaster/skill/mine_detect/icon.png");
        public static final ResourceLocation thunder_lance_icon = academy("textures/ability/electromaster/skill/thunder_lance/icon.png");
        public static final ResourceLocation electromagnetic_shield_icon = academy("textures/ability/electromaster/skill/electromagnetic_shield/icon.png");
        public static final ResourceLocation current_recharge_icon = academy("textures/ability/electromaster/skill/current_recharge/icon.png");
        public static final ResourceLocation current_symbiosis_icon = academy("textures/ability/electromaster/skill/current_symbiosis/icon.png");
        public static final ResourceLocation bioelectric_operation_icon = academy("textures/ability/electromaster/skill/bioelectric_operation/icon.png");
        public static final ResourceLocation iron_sand_arsenal_effect = academy("textures/ability/electromaster/skill/iron_sand_arsenal/effect/iron_sand.png");
        public static final ResourceLocation ball_lightning_icon = academy("textures/ability/electromaster/skill/ball_lightning/icon.png");
        public static final ResourceLocation single_high_speed_electron_beam_icon = academy("textures/ability/meltdowner/skill/single_high_speed_electron_beam/icon.png");
        public static final ResourceLocation scatter_bomb_icon = academy("textures/ability/meltdowner/skill/scatter_bomb/icon.png");
        public static final ResourceLocation radiation_intensify_icon = academy("textures/ability/meltdowner/skill/radiation_intensify/icon.png");
        public static final ResourceLocation mining_beam_icon = academy("textures/ability/meltdowner/skill/mining_beam/icon.png");
        public static final ResourceLocation light_shield_icon = academy("textures/ability/meltdowner/skill/light_shield/icon.png");
        public static final ResourceLocation light_shield_effect = academy("textures/ability/generic/skill/light_shield/effect/mdshield.png");
        public static final ResourceLocation particle_wave_cannon_icon = academy("textures/ability/meltdowner/skill/particle_wave_cannon/icon.png");
        public static final ResourceLocation jet_strike_icon = academy("textures/ability/meltdowner/skill/jet_strike/icon.png");
        public static final ResourceLocation auto_cruise_beam_cannon_icon = academy("textures/ability/meltdowner/skill/auto_cruise_beam_cannon/icon.png");
        public static final ResourceLocation threatening_teleport_icon = academy("textures/ability/teleport/skill/threatening_teleport/icon.png");
        public static final ResourceLocation space_folding_theorem_icon = academy("textures/ability/teleport/skill/space_folding_theorem/icon.png");
        public static final ResourceLocation self_teleport_icon = academy("textures/ability/teleport/skill/self_teleport/icon.png");
        public static final ResourceLocation piercing_teleportation_icon = academy("textures/ability/teleport/skill/piercing_teleportation/icon.png");
        public static final ResourceLocation flesh_ripping_icon = academy("textures/ability/teleport/skill/flesh_ripping/icon.png");
        public static final ResourceLocation location_teleport_icon = academy("textures/ability/teleport/skill/location_teleport/icon.png");
        public static final ResourceLocation quick_location_teleport_icon = academy("textures/ability/teleport/skill/quick_location_teleport/icon.png");
        public static final ResourceLocation area_teleport_select_icon = academy("textures/ability/teleport/skill/area_teleport_select/icon.png");
        public static final ResourceLocation flashing_icon = academy("textures/ability/teleport/skill/flashing/icon.png");
        public static final ResourceLocation defensive_teleport_icon = academy("textures/ability/teleport/skill/defensive_teleport/icon.png");
        public static final ResourceLocation chunk_leap_icon = academy("textures/ability/teleport/skill/chunk_leap/icon.png");
        public static final ResourceLocation teleport_cursor = academy("textures/ability/teleport/effect/teleport_cursor.png");
        public static final ResourceLocation darkmatter_shaping_icon = academy("textures/ability/darkmatter/skill/darkmatter_shaping/icon.png");
        public static final ResourceLocation darkmatter_disassemble_icon = academy("textures/ability/darkmatter/skill/darkmatter_disassemble/icon.png");
        public static final ResourceLocation darkmatter_phase_tuning_icon = academy("textures/ability/darkmatter/skill/darkmatter_phase_tuning/icon.png");
        public static final ResourceLocation darkmatter_cut_icon = academy("textures/ability/darkmatter/skill/darkmatter_cut/icon.png");
        public static final ResourceLocation darkmatter_cut_slash_effect_1 = academy("textures/ability/darkmatter/skill/darkmatter_cut/effect/darkmatter_cut_slash_1.png");
        public static final ResourceLocation darkmatter_cut_slash_effect_2 = academy("textures/ability/darkmatter/skill/darkmatter_cut/effect/darkmatter_cut_slash_2.png");
        public static final ResourceLocation darkmatter_cut_slash_effect_3 = academy("textures/ability/darkmatter/skill/darkmatter_cut/effect/darkmatter_cut_slash_3.png");
        public static final ResourceLocation darkmatter_cut_slash_effect_4 = academy("textures/ability/darkmatter/skill/darkmatter_cut/effect/darkmatter_cut_slash_4.png");
        public static final ResourceLocation darkmatter_radiation_icon = academy("textures/ability/darkmatter/skill/darkmatter_radiation/icon.png");
        public static final ResourceLocation darkmatter_repair_icon = academy("textures/ability/darkmatter/skill/darkmatter_repair/icon.png");
        public static final ResourceLocation darkmatter_creation_icon = academy("textures/ability/darkmatter/skill/darkmatter_creation/icon.png");
        public static final ResourceLocation darkmatter_beetle = academy("textures/entity/darkmatter_beetle.png");
        public static final ResourceLocation darkmatter_six_wings_icon = academy("textures/ability/darkmatter/skill/darkmatter_six_wings/icon.png");
        public static final ResourceLocation darkmatter_six_wings_effect = academy("textures/ability/darkmatter/skill/darkmatter_six_wings/effect/darkmatter_six_wings.png");
        public static final ResourceLocation reflection_filter_icon = academy("textures/ability/accelerator/skill/reflection_filter/icon.png");
        public static final ResourceLocation vector_blast_icon = academy("textures/ability/accelerator/skill/vector_blast/icon.png");
        public static final ResourceLocation kinetic_energy_applied_icon = academy("textures/ability/accelerator/skill/kinetic_energy_applied/icon.png");
        public static final ResourceLocation kinetic_throw_icon = academy("textures/ability/accelerator/skill/kinetic_throw/icon.png");
        public static final ResourceLocation black_wing_icon = academy("textures/ability/accelerator/skill/black_wing/icon.png");
        public static final ResourceLocation white_wing_icon = academy("textures/ability/accelerator/skill/white_wing/icon.png");
        public static final ResourceLocation platinum_wing_icon = academy("textures/ability/accelerator/skill/platinum_wing/icon.png");
        public static final ResourceLocation crossing_the_abyss_icon = academy("textures/ability/accelerator/skill/crossing_the_abyss/icon.png");
        public static final ResourceLocation plasma_generation_effect = academy("textures/ability/accelerator/skill/plasma_generation/effect/plasma.png");
        public static final ResourceLocation plasma_generation_cloud = academy("textures/ability/accelerator/skill/plasma_generation/effect/white_smoke_hq.png");
        public static final ResourceLocation dir_strike_icon = academy("textures/ability/accelerator/skill/dir_strike/icon.png");
        public static final ResourceLocation storm_wing = academy("textures/ability/accelerator/skill/storm_wing/effect/tornado_ring.png");
        public static final ResourceLocation black_wing = academy("textures/ability/accelerator/skill/black_wing/effect/tornado_ring.png");
        public static final ResourceLocation white_wing = academy("textures/ability/accelerator/skill/white_wing/effect/tornado_ring.png");
        public static final ResourceLocation white_wing_ascension = academy("textures/ability/accelerator/skill/white_wing/effect/ascension_ring.png");
        public static final ResourceLocation platinum_wing_starfield = academy("textures/ability/accelerator/skill/platinum_wing/effect/starfield.png");
        public static final ResourceLocation airflow_jet_icon = academy("textures/ability/aeromanip/skill/airflow_jet/icon.png");
        public static final ResourceLocation air_cushion_icon = academy("textures/ability/aeromanip/skill/air_cushion/icon.png");
        public static final ResourceLocation flow_sense_icon = academy("textures/ability/aeromanip/skill/flow_sense/icon.png");
        public static final ResourceLocation atmosphere_shield_icon = academy("textures/ability/aeromanip/skill/atmosphere_shield/icon.png");
        public static final ResourceLocation breathing_film_icon = academy("textures/ability/aeromanip/skill/breathing_film/icon.png");
        public static final ResourceLocation pneumatic_grasp_icon = academy("textures/ability/aeromanip/skill/pneumatic_grasp/icon.png");
        public static final ResourceLocation tailwind_field_icon = academy("textures/ability/aeromanip/skill/tailwind_field/icon.png");
        public static final ResourceLocation laminar_cutter_icon = academy("textures/ability/aeromanip/skill/laminar_cutter/icon.png");
        public static final ResourceLocation vortex_pull_icon = academy("textures/ability/aeromanip/skill/vortex_pull/icon.png");
        public static final ResourceLocation atmosphere_blast_gun_icon = academy("textures/ability/aeromanip/skill/atmosphere_blast_gun/icon.png");
        public static final ResourceLocation wind_corridor_icon = academy("textures/ability/aeromanip/skill/wind_corridor/icon.png");
        public static final ResourceLocation high_speed_jet_icon = wind_corridor_icon;
        public static final ResourceLocation pressure_lock_icon = academy("textures/ability/aeromanip/skill/pressure_lock/icon.png");
        public static final ResourceLocation flight_icon = academy("textures/ability/aeromanip/skill/flight/icon.png");
        public static final ResourceLocation vacuum_domain_icon = academy("textures/ability/aeromanip/skill/vacuum_domain/icon.png");
        public static final ResourceLocation atmospheric_dominion_icon = academy("textures/ability/aeromanip/skill/atmospheric_dominion/icon.png");
        public static final ResourceLocation adiabatic_compression_icon = atmospheric_dominion_icon;
        public static final ResourceLocation brain_domain_development_icon = academy("textures/ability/level0/skill/brain_domain_development/icon.png");
        public static final ResourceLocation multiple_brain_domain_segmentation_icon = academy("textures/ability/level0/skill/multiple_brain_domain_segmentation/icon.png");
        public static final ResourceLocation parallel_thought_computation_icon = academy("textures/ability/level0/skill/parallel_thought_computation/icon.png");
        public static final ResourceLocation complete_consciousness_analysis_icon = academy("textures/ability/level0/skill/complete_consciousness_analysis/icon.png");
        public static final ResourceLocation absolute_self_control_icon = academy("textures/ability/level0/skill/absolute_self_control/icon.png");
        public static final ResourceLocation endurance_training_icon = academy("textures/ability/level0/skill/endurance_training/icon.png");
        public static final ResourceLocation physical_training_icon = academy("textures/ability/level0/skill/physical_training/icon.png");
        public static final ResourceLocation output_control_icon = academy("textures/ability/level0/skill/output_control/icon.png");
        public static final ResourceLocation condition_any1 = academy("textures/ability/condition/any1.png");
        public static final ResourceLocation condition_any2 = academy("textures/ability/condition/any2.png");
        public static final ResourceLocation condition_any3 = academy("textures/ability/condition/any3.png");
        public static final ResourceLocation condition_any4 = academy("textures/ability/condition/any4.png");
        public static final ResourceLocation condition_any5 = academy("textures/ability/condition/any5.png");
        public static final ResourceLocation IMAG_PHASE_DOWSING_ROD = academy("textures/model/imag_phase_dowsing_rod.png");
        public static final ResourceLocation OMNI_CRAFTING_TABLE = academy("textures/model/omni_crafting_table.png");
        public static final ResourceLocation CAT_ENGINE = academy("textures/item/cat_engine.png");

        private textures() {
        }

        public static final class abilities {
            private abilities() {
            }

            public static final class condition {
                public static final ResourceLocation any1 = academy("textures/ability/condition/any1.png");
                public static final ResourceLocation any2 = academy("textures/ability/condition/any2.png");
                public static final ResourceLocation any3 = academy("textures/ability/condition/any3.png");
                public static final ResourceLocation any4 = academy("textures/ability/condition/any4.png");
                public static final ResourceLocation any5 = academy("textures/ability/condition/any5.png");

                private condition() {
                }
            }
        }

        public static final class ability {
            private ability() {
            }

            public static final class accelerator {
                public static final ResourceLocation icon = academy("textures/ability/accelerator/icon.png");
                public static final ResourceLocation icon_glow = academy("textures/ability/accelerator/icon_glow.png");
                public static final ResourceLocation icon_overlay = academy("textures/ability/accelerator/icon_overlay.png");

                private accelerator() {
                }

                public static final class skill {
                    private skill() {
                    }

                    public static final class bloodflow_reverse {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/bloodflow_reverse/icon.png");

                        private bloodflow_reverse() {
                        }
                    }

                    public static final class dir_strike {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/dir_strike/icon.png");

                        private dir_strike() {
                        }
                    }

                    public static final class directed_shock {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/directed_shock/icon.png");

                        private directed_shock() {
                        }
                    }

                    public static final class plasma_generation {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/plasma_generation/icon.png");

                        private plasma_generation() {
                        }
                    }

                    public static final class storm_wing {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/storm_wing/icon.png");

                        private storm_wing() {
                        }

                        public static final class effect {
                            public static final ResourceLocation tornado_ring = academy("textures/ability/accelerator/skill/storm_wing/effect/tornado_ring.png");

                            private effect() {
                            }
                        }
                    }

                    public static final class vec_accel {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/vec_accel/icon.png");

                        private vec_accel() {
                        }
                    }

                    public static final class vector_deviation {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/vector_deviation/icon.png");

                        private vector_deviation() {
                        }
                    }

                    public static final class vector_reflection {
                        public static final ResourceLocation icon = academy("textures/ability/accelerator/skill/vector_reflection/icon.png");

                        private vector_reflection() {
                        }
                    }
                }
            }

            public static final class condition {
                public static final ResourceLocation any1 = academy("textures/ability/condition/any1.png");
                public static final ResourceLocation any2 = academy("textures/ability/condition/any2.png");
                public static final ResourceLocation any3 = academy("textures/ability/condition/any3.png");
                public static final ResourceLocation any4 = academy("textures/ability/condition/any4.png");
                public static final ResourceLocation any5 = academy("textures/ability/condition/any5.png");

                private condition() {
                }
            }

            public static final class electromaster {
                public static final ResourceLocation icon = academy("textures/ability/electromaster/icon.png");
                public static final ResourceLocation icon_glow = academy("textures/ability/electromaster/icon_glow.png");
                public static final ResourceLocation icon_overlay = academy("textures/ability/electromaster/icon_overlay.png");

                private electromaster() {
                }

                public static final class skill {
                    private skill() {
                    }

                    public static final class arc_generate {
                        public static final ResourceLocation icon = academy("textures/ability/electromaster/skill/arc_generate/icon.png");

                        private arc_generate() {
                        }

                        public static final class effect {
                            public static final ResourceLocation line_segment = academy("textures/ability/electromaster/skill/arc_generate/effect/line_segment.png");

                            private effect() {
                            }
                        }
                    }

                    public static final class iron_sand_arsenal {
                        public static final ResourceLocation icon = academy("textures/ability/electromaster/skill/iron_sand_arsenal/icon.png");

                        private iron_sand_arsenal() {
                        }
                    }

                    public static final class sky_strike {
                        private sky_strike() {
                        }

                        public static final class effect {
                            public static final ResourceLocation lightning_column = academy("textures/ability/electromaster/skill/sky_strike/effect/lightning_column.png");
                            public static final ResourceLocation lightning_ribbon = academy("textures/ability/electromaster/skill/sky_strike/effect/lightning_ribbon.png");
                            public static final ResourceLocation impact_shockwave_ring = academy("textures/ability/electromaster/skill/sky_strike/effect/impact_shockwave_ring.png");
                            public static final ResourceLocation impact_flash = academy("textures/ability/electromaster/skill/sky_strike/effect/impact_flash.png");

                            private effect() {
                            }
                        }
                    }

                    public static final class railgun {
                        public static final ResourceLocation icon = academy("textures/ability/electromaster/skill/railgun/icon.png");

                        private railgun() {
                        }
                    }

                    public static final class thunderclap {
                        public static final ResourceLocation icon = academy("textures/ability/electromaster/skill/thunderclap/icon.png");

                        private thunderclap() {
                        }
                    }
                }
            }

            public static final class generic {
                private generic() {
                }

                public static final class effect {
                    public static final ResourceLocation glow_circle = academy("textures/ability/generic/effect/glow_circle.png");
                    public static final ResourceLocation smokes = academy("textures/ability/generic/effect/smokes.png");
                    public static final ResourceLocation sparkle_blurred = academy("textures/ability/generic/effect/sparkle_blurred.png");
                    public static final ResourceLocation white_smoke_hq = academy("textures/ability/generic/effect/white_smoke_hq.png");

                    private effect() {
                    }
                }
            }

            public static final class level0 {
                public static final ResourceLocation icon = academy("textures/ability/level0/icon.png");
                public static final ResourceLocation icon_glow = academy("textures/ability/level0/icon_glow.png");
                public static final ResourceLocation icon_overlay = academy("textures/ability/level0/icon_overlay.png");

                private level0() {
                }

                public static final class skill {
                    private skill() {
                    }

                    public static final class brain_domain_development {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/brain_domain_development/icon.png");

                        private brain_domain_development() {
                        }
                    }

                    public static final class multiple_brain_domain_segmentation {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/multiple_brain_domain_segmentation/icon.png");

                        private multiple_brain_domain_segmentation() {
                        }
                    }

                    public static final class parallel_thought_computation {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/parallel_thought_computation/icon.png");

                        private parallel_thought_computation() {
                        }
                    }

                    public static final class complete_consciousness_analysis {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/complete_consciousness_analysis/icon.png");

                        private complete_consciousness_analysis() {
                        }
                    }

                    public static final class absolute_self_control {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/absolute_self_control/icon.png");

                        private absolute_self_control() {
                        }
                    }

                    public static final class endurance_training {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/endurance_training/icon.png");

                        private endurance_training() {
                        }
                    }

                    public static final class physical_training {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/physical_training/icon.png");

                        private physical_training() {
                        }
                    }

                    public static final class output_control {
                        public static final ResourceLocation icon = academy("textures/ability/level0/skill/output_control/icon.png");

                        private output_control() {
                        }
                    }
                }
            }

            public static final class meltdowner {
                public static final ResourceLocation icon = academy("textures/ability/meltdowner/icon.png");
                public static final ResourceLocation icon_glow = academy("textures/ability/meltdowner/icon_glow.png");
                public static final ResourceLocation icon_overlay = academy("textures/ability/meltdowner/icon_overlay.png");

                private meltdowner() {
                }

                public static final class ray {
                    public static final ResourceLocation head = academy("textures/ability/meltdowner/ray/head.png");
                    public static final ResourceLocation hellflare_steam = academy("textures/ability/meltdowner/ray/hellflare_steam.png");
                    public static final ResourceLocation ray = academy("textures/ability/meltdowner/ray/ray.png");
                    public static final ResourceLocation tail = academy("textures/ability/meltdowner/ray/tail.png");

                    private ray() {
                    }
                }
            }

            public static final class mentalout {
                public static final ResourceLocation icon = academy("textures/ability/mentalout/icon.png");

                private mentalout() {
                }

                public static final class skill {
                    private skill() {
                    }

                    public static final class impression_manipulation {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/impression_manipulation/icon.png");

                        private impression_manipulation() {
                        }
                    }

                    public static final class mental_intervention {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/mental_intervention/icon.png");

                        private mental_intervention() {
                        }
                    }

                    public static final class mental_intrusion {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/mental_intrusion/icon.png");

                        private mental_intrusion() {
                        }
                    }

                    public static final class mental_takeover {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/mental_takeover/icon.png");

                        private mental_takeover() {
                        }
                    }

                    public static final class mental_stupor {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/mental_stupor/icon.png");

                        private mental_stupor() {
                        }
                    }

                    public static final class target_misidentification {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/target_misidentification/icon.png");

                        private target_misidentification() {
                        }
                    }

                    public static final class sensory_distortion {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/sensory_distortion/icon.png");

                        private sensory_distortion() {
                        }
                    }

                    public static final class precision_operation {
                        public static final ResourceLocation icon = academy("textures/ability/mentalout/skill/precision_operation/icon.png");

                        private precision_operation() {
                        }
                    }

                    public static final class wide_area_interference {
                        public static final ResourceLocation icon = precision_operation.icon;

                        private wide_area_interference() {
                        }
                    }
                }
            }

            public static final class teleport {
                public static final ResourceLocation icon = academy("textures/ability/teleport/icon.png");
                public static final ResourceLocation icon_glow = academy("textures/ability/teleport/icon_glow.png");
                public static final ResourceLocation icon_overlay = academy("textures/ability/teleport/icon_overlay.png");

                private teleport() {
                }
            }
        }

        public static final class gui {
            private gui() {
            }

            public static final class app {
                private app() {
                }

                public static final class abilitysettings {
                    public static final ResourceLocation ability_settings = academy("textures/gui/app/abilitysettings/ability_settings.png");

                    private abilitysettings() {
                    }
                }

                public static final class music {
                    public static final ResourceLocation cycle = academy("textures/gui/app/music/cycle.png");
                    public static final ResourceLocation icon = academy("textures/gui/app/music/icon.png");
                    public static final ResourceLocation next = academy("textures/gui/app/music/next.png");
                    public static final ResourceLocation now_playing = academy("textures/gui/app/music/now_playing.png");
                    public static final ResourceLocation pause = academy("textures/gui/app/music/pause.png");
                    public static final ResourceLocation play = academy("textures/gui/app/music/play.png");
                    public static final ResourceLocation previous = academy("textures/gui/app/music/previous.png");
                    public static final ResourceLocation random_play = academy("textures/gui/app/music/random_play.png");
                    public static final ResourceLocation single_cycle = academy("textures/gui/app/music/single_cycle.png");
                    public static final ResourceLocation volume = academy("textures/gui/app/music/volume.png");

                    private music() {
                    }
                }

                public static final class tutorial {
                    public static final ResourceLocation icon = academy("textures/gui/app/tutorial/icon.png");

                    private tutorial() {
                    }
                }
            }

            public static final class developer {
                public static final ResourceLocation button = academy("textures/gui/developer/button.png");
                public static final ResourceLocation button_learn = academy("textures/gui/developer/button_learn.png");
                public static final ResourceLocation effect_developer_background = academy("textures/gui/developer/effect_developer_background.png");
                public static final ResourceLocation line = academy("textures/gui/developer/line.png");
                public static final ResourceLocation parent_background = academy("textures/gui/developer/parent_background.png");
                public static final ResourceLocation parent_background_developerleft = academy("textures/gui/developer/parent_background_developerleft.png");
                public static final ResourceLocation parent_background_developermachine = academy("textures/gui/developer/parent_background_developermachine.png");
                public static final ResourceLocation parent_background_developerright = academy("textures/gui/developer/parent_background_developerright.png");
                public static final ResourceLocation skill_back = academy("textures/gui/developer/skill_back.png");
                public static final ResourceLocation skill_outline = academy("textures/gui/developer/skill_outline.png");
                public static final ResourceLocation skill_panel_back = academy("textures/gui/developer/skill_panel_back.png");
                public static final ResourceLocation skill_radial_mask = academy("textures/gui/developer/skill_radial_mask.png");
                public static final ResourceLocation skill_view_outline = academy("textures/gui/developer/skill_view_outline.png");
                public static final ResourceLocation skill_view_outline_glow = academy("textures/gui/developer/skill_view_outline_glow.png");
                public static final ResourceLocation ui_developerleft = academy("textures/gui/developer/ui_developerleft.png");
                public static final ResourceLocation ui_developerright = academy("textures/gui/developer/ui_developerright.png");

                private developer() {
                }
            }

            public static final class element {
                public static final ResourceLocation app_back = academy("textures/gui/element/app_back.png");
                public static final ResourceLocation element_background300x32 = academy("textures/gui/element/element_background300x32.png");
                public static final ResourceLocation element_background_light = academy("textures/gui/element/element_background_light.png");
                public static final ResourceLocation histogram = academy("textures/gui/element/histogram.png");
                public static final ResourceLocation line = academy("textures/gui/element/line.png");
                public static final ResourceLocation logo_tech = academy("textures/gui/element/logo_tech.png");
                public static final ResourceLocation ui_gen = academy("textures/gui/element/ui_gen.png");
                public static final ResourceLocation ui_inventory = academy("textures/gui/element/ui_inventory.png");
                /** White disc, tinted per entity category to draw map markers as small dots. */
                public static final ResourceLocation map_marker_dot = academy("textures/gui/map/marker_dot.png");

                private element() {
                }
            }

            public static final class icon {
                public static final ResourceLocation add = academy("textures/gui/icon/add.png");
                public static final ResourceLocation arrow_foward = academy("textures/gui/icon/arrow_foward.png");
                public static final ResourceLocation arrow_back = academy("textures/gui/icon/arrow_back.png");
                public static final ResourceLocation close = academy("textures/gui/icon/close.png");
                public static final ResourceLocation icon_accelerator = academy("textures/gui/icon/icon_accelerator.png");
                public static final ResourceLocation icon_box = academy("textures/gui/icon/icon_box.png");
                public static final ResourceLocation icon_connected = academy("textures/gui/icon/icon_connected.png");
                public static final ResourceLocation icon_cycle = academy("textures/gui/icon/icon_cycle.png");
                public static final ResourceLocation icon_electromaster = academy("textures/gui/icon/icon_electromaster.png");
                public static final ResourceLocation icon_inv = academy("textures/gui/icon/icon_inv.png");
                public static final ResourceLocation icon_meltdowner = academy("textures/gui/icon/icon_meltdowner.png");
                public static final ResourceLocation icon_nocategory = academy("textures/gui/icon/icon_nocategory.png");
                public static final ResourceLocation icon_node = academy("textures/gui/icon/icon_node.png");
                public static final ResourceLocation icon_random = academy("textures/gui/icon/icon_random.png");
                public static final ResourceLocation icon_settings = academy("textures/gui/icon/icon_settings.png");
                public static final ResourceLocation icon_teleporter = academy("textures/gui/icon/icon_teleporter.png");
                public static final ResourceLocation icon_tonode = academy("textures/gui/icon/icon_tonode.png");
                public static final ResourceLocation icon_unconnected = academy("textures/gui/icon/icon_unconnected.png");
                public static final ResourceLocation icon_wireless = academy("textures/gui/icon/icon_wireless.png");
                public static final ResourceLocation menu = academy("textures/gui/icon/menu.png");
                public static final ResourceLocation more = academy("textures/gui/icon/more.png");
                public static final ResourceLocation refresh = academy("textures/gui/icon/refresh.png");
                public static final ResourceLocation remove = academy("textures/gui/icon/remove.png");
                public static final ResourceLocation search = academy("textures/gui/icon/search.png");

                private icon() {
                }
            }

            public static final class node {
                public static final ResourceLocation state_node = academy("textures/gui/node/state_node.png");
                public static final ResourceLocation ui_node = academy("textures/gui/node/ui_node.png");

                private node() {
                }
            }

            public static final class omni_crafting {
                public static final ResourceLocation ui_omni_crafting = academy("textures/gui/omni_crafting/ui_omni_crafting.png");

                private omni_crafting() {
                }
            }

            public static final class solar_gen {
                public static final ResourceLocation icon_solar_gen_night = academy("textures/gui/solar_gen/icon_solar_gen_night.png");
                public static final ResourceLocation icon_solar_gen_rainy = academy("textures/gui/solar_gen/icon_solar_gen_rainy.png");
                public static final ResourceLocation icon_solar_gen_sunny = academy("textures/gui/solar_gen/icon_solar_gen_sunny.png");

                private solar_gen() {
                }
            }

            public static final class terminal {
                public static final ResourceLocation icon = academy("textures/gui/terminal/icon.png");

                private terminal() {
                }
            }

            public static final class wind_gen {
                public static final ResourceLocation icon_wind_base = academy("textures/gui/wind_gen/icon_wind_base.png");
                public static final ResourceLocation icon_wind_pillar = academy("textures/gui/wind_gen/icon_wind_pillar.png");
                public static final ResourceLocation icon_wind_top = academy("textures/gui/wind_gen/icon_wind_top.png");

                private wind_gen() {
                }
            }
        }

        public static final class hud {
            public static final ResourceLocation cp_bar_background = academy("textures/hud/cp_bar_background.png");
            public static final ResourceLocation cp_bar_value = academy("textures/hud/cp_bar_value.png");

            private hud() {
            }
        }

        public static final class model {
            public static final ResourceLocation ability_developer = academy("textures/model/ability_developer.png");
            public static final ResourceLocation ability_control_tablet = academy("textures/model/ability_control_tablet.png");
            public static final ResourceLocation cleaning_robot = academy("textures/model/cleaning_robot.png");
            public static final ResourceLocation omni_crafting_table = academy("textures/model/omni_crafting_table.png");
            public static final ResourceLocation solar_gen = academy("textures/model/solar_gen.png");
            public static final ResourceLocation wind_gen = academy("textures/model/wind_gen.png");
            public static final ResourceLocation wind_gen_top = academy("textures/model/wind_gen_top.png");
            public static final ResourceLocation wireless_node = academy("textures/model/wireless_node.png");

            private model() {
            }
        }

        public static final class block {
            public static final ResourceLocation wind_gen_pillar = academy("textures/block/wind_gen_pillar.png");

            private block() {
            }
        }

        public static final class item {
            public static final ResourceLocation cat_engine = academy("textures/item/cat_engine.png");

            private item() {
            }
        }
    }
}
