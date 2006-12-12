package com.finalist.cmsc.resources.forms;


@SuppressWarnings("serial")
public class ImageForm extends SearchForm {
    
    private String title;
    private String description;

    public ImageForm() {
        super("images");
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
