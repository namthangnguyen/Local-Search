from __future__ import print_function
from ortools.linear_solver import pywraplp

def print_data(days, time_slots, num_rooms, 
            num_classes, num_teachers, room_caps,
            class_student, class_unit, class_teacher):
    print("The number of day: ", days)
    print("The number of timeslot: ", time_slots)
    print("The number of classroom: ", num_rooms)
    print("The number of class: ", num_classes)
    print("The number of teacher: ", num_teachers)
    for i in range(num_rooms):
        print("Room: {} has {} seats".format(i, room_caps[i]))
    for i in range(num_classes):
        print("Class: {} has {} students".format(i, class_student[i]))
    for i in range(num_classes):
        print("Class: {} has {} units".format(i, class_unit[i]))
    for i in range(num_classes):
        print("Class: {} with teacher {}".format(i, class_teacher[i]))
    

def read_data(data_path):
    days = None
    time_slots = None
    num_rooms = None
    num_classes = None
    num_teachers = None
    room_caps = []
    class_student = []
    class_unit = []
    class_teacher = []
    with open(data_path, 'r') as f:
        lines = f.read().split('\n')
        for i in range(len(lines)):
            lines[i] = lines[i].split(" ")
        days = int(lines[0][0])
        time_slots = int(lines[1][0])
        num_rooms = int(lines[2][0])
        num_classes = int(lines[3][0])
        num_teachers = int(lines[4][0])
        room_caps = [int(cap) for cap in lines[5]]
        class_student = [int(s) for s in lines[6]]
        class_unit = [int(u) for u in lines[7]]
        class_teacher = [int(t) for t in lines[8]]
        # start_time = [int(b) for b in lines[9]]


    print_data(days, time_slots, num_rooms, 
            num_classes, num_teachers, room_caps,
            class_student, class_unit, class_teacher)

    return (days, time_slots, num_rooms, num_classes, 
            num_teachers, room_caps, class_student, class_unit, class_teacher)


def main(data_path):
    (days, time_slots, num_rooms, num_classes, num_teachers,\
        room_caps, class_student, class_unit, class_teacher) = read_data(data_path)
    all_shifts = range(days*2)     # each day has two shifts
    all_timeslots = range(int(time_slots/2))    # devide timeslot into two shifts
    all_rooms = range(num_rooms)
    all_classes = range(num_classes)
    all_teachers = range(num_teachers)
    ClassTeacher = [[0 for _ in all_teachers] for _ in all_classes]
    # StartTime = [[0 for _ in all_timeslots] for _ in all_classes]
    for n in all_classes:
        ClassTeacher[n][class_teacher[n]] = 1
    # print(ClassTeacher)
    # for n in all_classes:
    #     StartTime[n][start_time[n]] = 1
    # print(ClassTeacher)

    # ==================================================
    # Creates the model
    model = pywraplp.Solver('Scheduling',
                           pywraplp.Solver.CBC_MIXED_INTEGER_PROGRAMMING)
    
    # ==================================================
    # Creates model's variables
    ClassRoom = {}    # ClassTime[n,m] = 1 if class n is assigned in shift d and room m
    for n in all_classes:
        for m in all_rooms:
            ClassRoom[n,m] = model.BoolVar('ClassRoom_%i_%i' %(n, m))

    StartTime = {}      # StartTime[n,s] = 1 if class n start at unit time s
    for n in all_classes:
        for s in all_timeslots:
            StartTime[n,s] = model.BoolVar('StartTime_%i_%i' % (n,s))

    ClassDay = {}       # ClassDay[n,d] = 1 if class n is assigned to shift d
    for n in all_classes:
        for d in all_shifts:
            ClassDay[n,d] = model.BoolVar('ClassDay_%i_%i' % (n,d))

    ClassTime = {}      # ClassTime[n,s] = 1 if class n is assigned to unit time s
    for n in all_classes:
        for s in all_timeslots:
            ClassTime[n,s] = model.BoolVar('ClassTime_%i_%i' % (n,s))

    RoomUsed = {}       # RoomUsed[m] = 1 if room m is used
    for m in all_rooms:
        RoomUsed[m] = model.BoolVar('RoomUsed_%i' %m)
 
    # ==================================================
    # Creates model's constraints
    # Each class is only taught in one shift
    for n in all_classes:
        # print("Add class constraint")
        model.Add(model.Sum([ClassDay[n,d] for d in all_shifts]) == 1)

    # The number of Class's student has to less or equal than room's capacity
    for n in all_classes:
        for m in all_rooms:
            # print("Add capacity constraint")
            model.Add((room_caps[m] - class_student[n])*ClassRoom[n,m] >= 0)
            model.Add(ClassRoom[n,m] <= RoomUsed[m])

    # Each class has one room
    for n in all_classes:
        model.Add(model.Sum([ClassRoom[n,m] for m in all_rooms]) == 1)

    
    for n in all_classes:
        un = class_unit[n]

        # And no class's time exceed unit 6
        model.Add(un + model.Sum([StartTime[n,s]*s for s in all_timeslots]) <= 6)
        # Only one start time
        model.Add(model.Sum([StartTime[n,s] for s in all_timeslots]) == 1)
        # Sum of ClassTime of class n has to equal to class_unit[n] 
        model.Add(model.Sum([ClassTime[n,s] for s in all_timeslots]) == un)

        for s in all_timeslots:
            if s + un <= 6:
                # Class happens continously
                for i in range(un): 
                    model.Add(StartTime[n,s] <= ClassTime[n,s+i])

    # Two diffent classes need two different rooms at a time
    bigM = 100
    for n1 in range(num_classes-1):
        for n2 in range(n1+1, num_classes):
            for m in all_rooms:
                for d in all_shifts:
                    for s in all_timeslots:
                        model.Add((ClassRoom[n1,m]*bigM + 
                                ClassRoom[n2,m]*bigM +
                                ClassDay[n1,d] + 
                                ClassDay[n2,d] +
                                ClassTime[n1,s] + 
                                ClassTime[n2,s]) <= 2*bigM + 3
                        )

    # Two different classes need two different teachers at a time
    for n1 in range(num_classes-1):
        for n2 in range(n1+1, num_classes):
            for t in all_teachers:
                if ClassTeacher[n1][t] == 1 and ClassTeacher[n1][t] == ClassTeacher[n2][t]:
                    for d in all_shifts:
                        for s in all_timeslots:
                            model.Add((ClassDay[n1,d] + 
                                    ClassDay[n2,d] +
                                    ClassTime[n1,s] + 
                                    ClassTime[n2,s]) <= 3
                            )

    # print(ClassRoom)

    # print(ClassRoom[0,0].solution_value())
    model.Minimize(model.Sum([RoomUsed[m] for m in all_rooms]))
    print('Number of variables =', model.NumVariables())
    print('Number of constraints =', model.NumConstraints())
    # Solver
    status = model.Solve() 
    if status == pywraplp.Solver.OPTIMAL or status == pywraplp.Solver.FEASIBLE:
        for d in all_shifts:
            print("Day: " + str(int(d/2)) + " shift: " + str(int(d%2)))
            for n in all_classes:
                if ClassDay[n,d].solution_value() == 1:
                    for s in all_timeslots:
                        if ClassTime[n,s].solution_value() == 1:
                            for m in all_rooms:
                                if ClassRoom[n,m].solution_value() == 1:
                                    print("Class", str(n), "Room", str(m), "TimeSlot", str(s), sep=" ")

    else:
        print("No solution")
    
    print('\nAdvanced usage:')
    print('Problem solved in %f milliseconds' % model.wall_time())
    print('Problem solved in %d iterations' % model.iterations())
    print('Problem solved in %d branch-and-bound nodes' % model.nodes())


if __name__ == '__main__':
    data_path = "./data/10_3_3"
    # read_data(data_path)
    main(data_path)