use std::cmp::Ordering;

/// Represents a day of the week
#[derive(Clone, Copy, Debug, PartialEq)]
pub enum Day {
    Mon = 0,
    Tues,
    Wed,
    Thurs,
}

impl Day {
    #[allow(dead_code)]
    pub fn as_num(self) -> u8 {
        self as u8
    }
}

/// Represents a time of day
#[derive(Clone, Copy, Debug)]
pub struct Time(pub u16);

impl PartialEq for Time {
    fn eq(&self, other: &Self) -> bool {
        self.0 == other.0
    }
}

impl PartialOrd for Time {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        self.0.partial_cmp(&other.0)
    }
}

impl From<u16> for Time {
    fn from(num: u16) -> Self {
        Self(num)
    }
}
