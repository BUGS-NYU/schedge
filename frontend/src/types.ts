export type Days = "Mon" | "Tues" | "Wed" | "Thurs";

export interface APIMeeting {
  crn: number;
  days: [Days, Days];
  endTime: number;
  startTime: number;
  professor: string;
}

export interface Meeting {
  title: string;
  startDate: Date;
  endDate: Date;
}
