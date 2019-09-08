export type Days = "Mon" | "Tues" | "Wed" | "Thurs";

export interface APIMeeting {
  crn: number;
  days: [Days, Days];
  endTime: number;
  startTime: number;
  professor: string;
  courseName: string;
  location: string;
}

export interface Meeting {
  title: string;
  startDate: Date;
  endDate: Date;
  location: string;
  professor: string;
}

export interface APICourse {
  id: number;
  departmentId: number;
  name: string;
  prerequisites: Array<APICourse>;
}
