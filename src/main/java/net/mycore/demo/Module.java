package net.mycore.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    @Column(unique = true)
    private String basePackage;

    private String description;

    //@Pattern(regexp = "\\d+\\..*")
    private String version;

    private ZonedDateTime buildTime;

    @NotNull
    private Boolean active = true;

    private String artifact;

    private String umdLocation;

    private String moduleMap;

    private Boolean inError;

    private Boolean installOnBoot;
    @ElementCollection
    private List<String> dependencies;

    @Transient
    private PluginInterface pluginInstance;

    /*private int status;

    private int archived;

    private Boolean processConfig;*/

    private Integer priority = 100;

//    @OneToOne(mappedBy = "module", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private ModuleArtifact moduleArtifact;
//
//    @OneToMany(mappedBy = "module", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
//    private Set<WebModule> webModules = new HashSet<>();
//
//    @OneToMany(mappedBy = "module", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private Set<Menu> menus = new HashSet<>();
//
//    @OneToMany(mappedBy = "module", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
//    @JsonIgnore
//    private Set<Authority> bundledAuthorities = new HashSet<>();
//
//    @OneToMany(mappedBy = "module", cascade = {CascadeType.ALL})
//    @JsonIgnore
//    private Set<ModuleDependency> dependencies = new HashSet<>();
//
//    @OneToMany(mappedBy = "module", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
//    @JsonIgnore
//    private Set<Form> templates = new HashSet<>();
//    @Override
//    public boolean isNew() {
//        return id == null;
//    }
//    public enum Type {ERROR, SUCCESS, WARNING}
//    @Transient
//    private Type type;
//    @Transient
//    private String message;
//
//    @OneToMany(mappedBy = "module", cascade = {CascadeType.ALL})
//    private Set<Permission> Permissions = new HashSet<>();
}
