package com.annalogue.m2.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publications")
public class PublicationEntity {

	public PublicationEntity() {
	}

	public PublicationEntity(String name, String description, FileType fileType, ModuleEntity module, UserEntity author, String filePath) {
		this.name = name;
		this.description = description;
		this.fileType = fileType;
		this.module = module;
		this.author = author;
		this.filePath = filePath;
		this.comments = new ArrayList<>();
		this.corrections = new ArrayList<>();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", nullable = true) // can be null
	private String description;

	@Column(name = "file_type", nullable = false)
	private FileType fileType;

	@ManyToOne
	private ModuleEntity module;

	@ManyToOne
	private UserEntity author;

	@Column(name = "file_path", nullable = false)
	private String filePath;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CommentEntity> comments;

	@OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CorrectionEntity> corrections;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public ModuleEntity getModule() {
		return module;
	}

	public void setModule(ModuleEntity module) {
		this.module = module;
	}

	public UserEntity getAuthor() {
		return author;
	}

	public void setAuthor(UserEntity author) {
		this.author = author;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setComments(List<CommentEntity> comments) {
		this.comments = comments;
	}

	public List<CommentEntity> getComments() {
		return comments;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<CorrectionEntity> getCorrections() {
		return corrections;
	}

	public void setCorrections(List<CorrectionEntity> corrections) {
		this.corrections = corrections;
	}
}
