#[derive(Clone, Debug)]
pub struct Meeting {
    pub days: (u8, u8),
    pub start_time: u16,
    pub end_time: u16,
    pub professor: String,
}
