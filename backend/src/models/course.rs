#[derive(Debug, Clone, Serialize)]
pub struct CourseOutput {
    pub id: usize,
    pub prerequisites: Vec<usize>,
    pub name: &'static str,
    pub department_id: usize,
}

#[derive(Debug, Clone)]
pub struct Course {
    pub prerequisites: Vec<usize>,
    pub name: &'static str,
    pub department_id: usize,
}

impl Course {
    pub fn as_output(self, id: usize) -> CourseOutput {
        CourseOutput {
            id,
            prerequisites: self.prerequisites,
            name: self.name,
            department_id: self.department_id,
        }
    }

    #[allow(dead_code)]
    pub fn to_output(&self, id: usize) -> CourseOutput {
        CourseOutput {
            id,
            prerequisites: self.prerequisites.clone(),
            name: self.name,
            department_id: self.department_id,
        }
    }
}
