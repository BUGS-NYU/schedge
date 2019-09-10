#![allow(dead_code)]

/// Term that a class can be taken in.
pub enum Term {
    January = 2,
    Spring = 4,
    Summer = 6,
    Fall = 8,
}

/// TermID, used for interacting with NYU services.
pub struct TermID {
    /// Term
    term: Term,
    /// Year, expressed as the current year relative to the year 1900 (e.g. 2019 is 119)
    year: u16,
}

impl TermID {
    pub fn as_u16(self) -> u16 {
        self.year * 10 + self.term as u16
    }
}

/// Subject string. Doubles as school as well.
pub enum Subject {
    CsciUa,
    MathUa,
}

impl Subject {
    pub fn to_string(&self) -> String {
        match self {
            Self::CsciUa => "CSCI-UA".into(),
            Self::MathUa => "MATH-UA".into(),
        }
    }
    pub fn from(string: &str) -> Option<Subject> {
        Some(match string {
            "CSCI-UA" => Self::CsciUa,
            "MATH-UA" => Self::MathUa,
            _ => return None,
        })
    }
    pub fn school(&self) -> School {
        match self {
            Self::CsciUa | Self::MathUa => School::UA,
        }
    }
}

pub enum School {
    UA,
    GU,
}

impl School {
    pub fn to_string(&self) -> String {
        match self {
            Self::UA => "UA".into(),
            Self::GU => "UA".into(),
        }
    }
    fn from(string: &str) -> Option<School> {
        Some(match string {
            "UA" => Self::UA,
            "GU" => Self::GU,
            _ => return None,
        })
    }
}
