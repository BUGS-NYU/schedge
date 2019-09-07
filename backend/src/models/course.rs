#[derive(Debug, Clone)]
pub struct Course {
    pub prerequisites: Vec<u32>,
    pub name: &'static str,
}
